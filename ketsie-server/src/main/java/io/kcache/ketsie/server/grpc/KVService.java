/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.kcache.ketsie.server.grpc;

import com.google.protobuf.ByteString;
import io.etcd.jetcd.api.CompactionRequest;
import io.etcd.jetcd.api.CompactionResponse;
import io.etcd.jetcd.api.Compare;
import io.etcd.jetcd.api.DeleteRangeRequest;
import io.etcd.jetcd.api.DeleteRangeResponse;
import io.etcd.jetcd.api.KVGrpc;
import io.etcd.jetcd.api.KeyValue;
import io.etcd.jetcd.api.PutRequest;
import io.etcd.jetcd.api.PutResponse;
import io.etcd.jetcd.api.RangeRequest;
import io.etcd.jetcd.api.RangeResponse;
import io.etcd.jetcd.api.RequestOp;
import io.etcd.jetcd.api.ResponseOp;
import io.etcd.jetcd.api.TxnRequest;
import io.etcd.jetcd.api.TxnResponse;
import io.grpc.stub.StreamObserver;
import io.kcache.KeyValueIterator;
import io.kcache.ketsie.KetsieEngine;
import io.kcache.ketsie.version.TxVersionedCache;
import io.kcache.ketsie.version.VersionedCache;
import io.kcache.ketsie.version.VersionedValue;
import org.apache.omid.transaction.RollbackException;
import org.apache.omid.transaction.Transaction;
import org.apache.omid.transaction.TransactionException;
import org.apache.omid.transaction.TransactionManager;

import java.util.ArrayList;
import java.util.List;

public class KVService extends KVGrpc.KVImplBase {

    @Override
    public void range(RangeRequest request, StreamObserver<RangeResponse> responseObserver) {
        // TODO both key and range_end \0
        // TODO limit, keys_only, count_only
        // TODO error on sort_target
        // TODO more
        TransactionManager txMgr = KetsieEngine.getInstance().getTxManager();
        Transaction tx = null;
        try {
            tx = txMgr.begin();
            RangeResponse response = doRange(request);
            txMgr.commit(tx);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (TransactionException | RollbackException e) {
            if (tx != null) {
                try {
                    txMgr.rollback(tx);
                } catch (TransactionException te) {
                    // ignore
                }
            }
            // TODO
        }
    }

    private RangeResponse doRange(RangeRequest request) {
        TxVersionedCache cache = KetsieEngine.getInstance().getTxCache();
        byte[] from = request.getKey().toByteArray();
        byte[] to = request.getRangeEnd().toByteArray();
        boolean descending = request.getSortOrder() == RangeRequest.SortOrder.DESCEND;
        RangeResponse.Builder responseBuilder = RangeResponse.newBuilder();
        if (to.length > 0) {
            long count = 0L;
            try (KeyValueIterator<byte[], VersionedValue> iter = cache.range(from, true, to, false, descending)) {
                while (iter.hasNext()) {
                    io.kcache.KeyValue<byte[], VersionedValue> entry = iter.next();
                    KeyValue kv = toKeyValue(entry.key, entry.value);
                    responseBuilder.addKvs(kv);
                    count++;
                }
            }
            responseBuilder.setCount(count);
        } else {
            VersionedValue versioned = cache.get(from);
            if (versioned != null) {
                KeyValue kv = toKeyValue(from, versioned);
                responseBuilder.addKvs(kv);
                responseBuilder.setCount(1L);
            }

        }
        return responseBuilder.build();
    }

    @Override
    public void put(PutRequest request, StreamObserver<PutResponse> responseObserver) {
        TransactionManager txMgr = KetsieEngine.getInstance().getTxManager();
        Transaction tx = null;
        try {
            tx = txMgr.begin();
            PutResponse response = doPut(request);
            txMgr.commit(tx);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (TransactionException | RollbackException e) {
            if (tx != null) {
                try {
                    txMgr.rollback(tx);
                } catch (TransactionException te) {
                    // ignore
                }
            }
            // TODO
        }
    }

    private PutResponse doPut(PutRequest request) {
        TxVersionedCache cache = KetsieEngine.getInstance().getTxCache();
        byte[] key = request.getKey().toByteArray();
        byte[] value = request.getValue().toByteArray();
        VersionedValue versioned = cache.get(key);
        byte[] oldValue = versioned != null ? versioned.getValue() : null;
        if (!request.getIgnoreValue()) {
            cache.replace(key, oldValue, value);
        }
        PutResponse.Builder responseBuilder = PutResponse.newBuilder();
        if (request.getPrevKv() && versioned != null) {
            KeyValue kv = toKeyValue(key, versioned);
            responseBuilder.setPrevKv(kv);
        }
        return responseBuilder.build();
    }

    @Override
    public void deleteRange(DeleteRangeRequest request, StreamObserver<DeleteRangeResponse> responseObserver) {
        // TODO both key and range_end \0
        TransactionManager txMgr = KetsieEngine.getInstance().getTxManager();
        Transaction tx = null;
        try {
            tx = txMgr.begin();
            DeleteRangeResponse response = doDeleteRange(request);
            txMgr.commit(tx);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (TransactionException | RollbackException e) {
            if (tx != null) {
                try {
                    txMgr.rollback(tx);
                } catch (TransactionException te) {
                    // ignore
                }
            }
            // TODO
        }
    }

    private DeleteRangeResponse doDeleteRange(DeleteRangeRequest request) {
        TxVersionedCache cache = KetsieEngine.getInstance().getTxCache();
        byte[] from = request.getKey().toByteArray();
        byte[] to = request.getRangeEnd().toByteArray();
        List<byte[]> keys = new ArrayList<>();
        DeleteRangeResponse.Builder responseBuilder = DeleteRangeResponse.newBuilder();
        if (to.length > 0) {
            long count = 0L;
            try (KeyValueIterator<byte[], VersionedValue> iter = cache.range(from, true, to, false)) {
                while (iter.hasNext()) {
                    io.kcache.KeyValue<byte[], VersionedValue> entry = iter.next();
                    keys.add(entry.key);
                    if (request.getPrevKv()) {
                        KeyValue kv = toKeyValue(entry.key, entry.value);
                        responseBuilder.addPrevKvs(kv);
                    }
                    count++;
                }
            }
            responseBuilder.setDeleted(count);
        } else {
            VersionedValue versioned = cache.get(from);
            if (versioned != null) {
                keys.add(from);
                if (request.getPrevKv()) {
                    KeyValue kv = toKeyValue(from, versioned);
                    responseBuilder.addPrevKvs(kv);
                }
                responseBuilder.setDeleted(1);
            }
        }
        cache.remove(keys);
        return responseBuilder.build();
    }

    @Override
    public void txn(TxnRequest request, StreamObserver<TxnResponse> responseObserver) {
        // TODO check cmp < value
        // TODO both key and range_end \0
        TransactionManager txMgr = KetsieEngine.getInstance().getTxManager();
        Transaction tx = null;
        try {
            tx = txMgr.begin();
            TxnResponse response = doTxn(request);
            txMgr.commit(tx);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (TransactionException | RollbackException e) {
            if (tx != null) {
                try {
                    txMgr.rollback(tx);
                } catch (TransactionException te) {
                    // ignore
                }
            }
            // TODO
        }
    }

    private TxnResponse doTxn(TxnRequest request) {
        boolean succeeded = doCompares(request.getCompareList());
        List<ResponseOp> responses = doRequests(succeeded ? request.getSuccessList() : request.getFailureList());
        return TxnResponse.newBuilder()
            .setSucceeded(succeeded)
            .addAllResponses(responses)
            .build();
    }

    private boolean doCompares(List<Compare> compares) {
        for (Compare compare : compares) {
            if (!doCompare(compare)) {
                return false;
            }
        }
        return true;
    }

    private boolean doCompare(Compare compare) {
        TxVersionedCache cache = KetsieEngine.getInstance().getTxCache();
        Compare.CompareTarget target = compare.getTarget();
        if (target != Compare.CompareTarget.VALUE) {
            // TODO
            throw new IllegalArgumentException();
        }
        byte[] from = compare.getKey().toByteArray();
        byte[] to = compare.getRangeEnd().toByteArray();
        if (to.length > 0) {
            try (KeyValueIterator<byte[], VersionedValue> iter = cache.range(from, true, to, false)) {
                while (iter.hasNext()) {
                    io.kcache.KeyValue<byte[], VersionedValue> entry = iter.next();
                    if (!doCompareOne(compare, entry.value)) {
                        return false;
                    }
                }
            }
            return true;
        } else {
            VersionedValue versioned = cache.get(from);
            return doCompareOne(compare, versioned);
        }
    }

    private boolean doCompareOne(Compare compare, VersionedValue versioned) {
        byte[] value = compare.getValue().toByteArray();
        Integer cmp = versioned != null ? VersionedCache.BYTES_COMPARATOR.compare(versioned.getValue(), value) : null;
        switch (compare.getResult()) {
            case EQUAL:
                return cmp != null ? cmp == 0 : value == null || value.length == 0;
            case GREATER:
                return cmp != null && cmp > 0;
            case LESS:
                return cmp != null && cmp < 0;
            case NOT_EQUAL:
                return cmp != null ? cmp != 0 : value != null && value.length != 0;
            default:
                // TODO
                throw new IllegalArgumentException();
        }
    }

    private List<ResponseOp> doRequests(List<RequestOp> requests) {
        List<ResponseOp> responses = new ArrayList<>();
        for (RequestOp request : requests) {
            responses.add(doRequest(request));
        }
        return responses;
    }

    private ResponseOp doRequest(RequestOp request) {
        ResponseOp.Builder responseBuilder = ResponseOp.newBuilder();
        switch (request.getRequestCase()) {
            case REQUEST_RANGE:
                return responseBuilder.setResponseRange(doRange(request.getRequestRange())).build();
            case REQUEST_PUT:
                return responseBuilder.setResponsePut(doPut(request.getRequestPut())).build();
            case REQUEST_DELETE_RANGE:
                return responseBuilder.setResponseDeleteRange(doDeleteRange(request.getRequestDeleteRange())).build();
            case REQUEST_TXN:
                return responseBuilder.setResponseTxn(doTxn(request.getRequestTxn())).build();
            case REQUEST_NOT_SET:
                return responseBuilder.build();
            default:
                // TODO
                throw new IllegalArgumentException();
        }
    }

    @Override
    public void compact(CompactionRequest request, StreamObserver<CompactionResponse> responseObserver) {
        super.compact(request, responseObserver);
    }

    private static KeyValue toKeyValue(byte[] key, VersionedValue versioned) {
        return KeyValue.newBuilder()
            .setKey(ByteString.copyFrom(key))
            .setValue(ByteString.copyFrom(versioned.getValue()))
            .setModRevision(versioned.getVersion())
            .build();
    }
}

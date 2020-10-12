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
package io.kcache.ketsie.server.kv;

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

public class KVImpl extends KVGrpc.KVImplBase {

    @Override
    public void range(RangeRequest request, StreamObserver<RangeResponse> responseObserver) {
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
        RangeResponse.Builder responseBuilder = RangeResponse.newBuilder();
        if (to.length > 0) {
            // TODO handle to = new byte[]{0}
            try (KeyValueIterator<byte[], VersionedValue> iter = cache.range(from, true, to, false)) {
                while (iter.hasNext()) {
                    io.kcache.KeyValue<byte[], VersionedValue> entry = iter.next();
                    KeyValue kv = KeyValue.newBuilder()
                        .setKey(ByteString.copyFrom(entry.key))
                        .setValue(ByteString.copyFrom(entry.value.getValue()))
                        .build();
                    responseBuilder.addKvs(kv);
                }
            }
        } else {
            VersionedValue versioned = cache.get(from);
            if (versioned != null) {
                KeyValue kv = KeyValue.newBuilder()
                    .setKey(ByteString.copyFrom(from))
                    .setValue(ByteString.copyFrom(versioned.getValue()))
                    .build();
                responseBuilder.addKvs(kv);
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
        cache.replace(key, oldValue, value);
        PutResponse.Builder responseBuilder = PutResponse.newBuilder();
        if (versioned != null) {
            KeyValue kv = KeyValue.newBuilder()
                .setKey(ByteString.copyFrom(key))
                .setValue(ByteString.copyFrom(oldValue))
                .build();
            responseBuilder.setPrevKv(kv);
        }
        return responseBuilder.build();
    }

    @Override
    public void deleteRange(DeleteRangeRequest request, StreamObserver<DeleteRangeResponse> responseObserver) {
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
            // TODO handle to = new byte[]{0}
            try (KeyValueIterator<byte[], VersionedValue> iter = cache.range(from, true, to, false)) {
                while (iter.hasNext()) {
                    io.kcache.KeyValue<byte[], VersionedValue> entry = iter.next();
                    keys.add(entry.key);
                    KeyValue kv = KeyValue.newBuilder()
                        .setKey(ByteString.copyFrom(entry.key))
                        .setValue(ByteString.copyFrom(entry.value.getValue()))
                        .build();
                    responseBuilder.addPrevKvs(kv);
                }
            }
        } else {
            VersionedValue versioned = cache.get(from);
            if (versioned != null) {
                keys.add(from);
                KeyValue kv = KeyValue.newBuilder()
                    .setKey(ByteString.copyFrom(from))
                    .setValue(ByteString.copyFrom(versioned.getValue()))
                    .build();
                responseBuilder.addPrevKvs(kv);
            }
        }
        cache.remove(keys);
        return responseBuilder.build();
    }

    @Override
    public void txn(TxnRequest request, StreamObserver<TxnResponse> responseObserver) {
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
        byte[] key = compare.getKey().toByteArray();
        byte[] value = compare.getValue().toByteArray();
        VersionedValue versioned = cache.get(key);
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
}

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
package io.kcache.keta.server.grpc;

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
import io.etcd.jetcd.api.ResponseHeader;
import io.etcd.jetcd.api.ResponseOp;
import io.etcd.jetcd.api.TxnRequest;
import io.etcd.jetcd.api.TxnResponse;
import io.grpc.stub.StreamObserver;
import io.kcache.KeyValueIterator;
import io.kcache.keta.KetaEngine;
import io.kcache.keta.lease.KetaLeaseManager;
import io.kcache.keta.lease.LeaseKeys;
import io.kcache.keta.transaction.client.KetaTransaction;
import io.kcache.keta.utils.ProtoUtils;
import io.kcache.keta.version.TxVersionedCache;
import io.kcache.keta.version.VersionedCache;
import io.kcache.keta.version.VersionedValue;
import org.apache.kafka.common.utils.Bytes;
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
        TransactionManager txMgr = KetaEngine.getInstance().getTxManager();
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
            throw new IllegalStateException(e);
        }
    }

    private RangeResponse doRange(RangeRequest request) {
        TxVersionedCache cache = KetaEngine.getInstance().getTxCache();
        byte[] from = request.getKey().toByteArray();
        byte[] to = request.getRangeEnd().toByteArray();
        boolean descending = request.getSortOrder() == RangeRequest.SortOrder.DESCEND;
        RangeResponse.Builder responseBuilder = RangeResponse.newBuilder();
        responseBuilder.setHeader(toResponseHeader());
        if (to.length > 0) {
            long count = 0L;
            try (KeyValueIterator<byte[], VersionedValue> iter = cache.range(from, true, to, false, descending)) {
                while (iter.hasNext()) {
                    io.kcache.KeyValue<byte[], VersionedValue> entry = iter.next();
                    KeyValue kv = ProtoUtils.toKeyValue(entry.key, entry.value);
                    responseBuilder.addKvs(kv);
                    count++;
                }
            }
            responseBuilder.setCount(count);
        } else {
            VersionedValue versioned = cache.get(from);
            if (versioned != null) {
                KeyValue kv = ProtoUtils.toKeyValue(from, versioned);
                responseBuilder.addKvs(kv);
                responseBuilder.setCount(1L);
            }

        }
        return responseBuilder.build();
    }

    @Override
    public void put(PutRequest request, StreamObserver<PutResponse> responseObserver) {
        TransactionManager txMgr = KetaEngine.getInstance().getTxManager();
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
            throw new IllegalStateException(e);
        }
    }

    private PutResponse doPut(PutRequest request) {
        TxVersionedCache cache = KetaEngine.getInstance().getTxCache();
        KetaLeaseManager leaseMgr = KetaEngine.getInstance().getLeaseManager();
        byte[] key = request.getKey().toByteArray();
        byte[] value = request.getValue().toByteArray();
        long lease = request.getLease();
        VersionedValue versioned = null;
        if (!request.getIgnoreValue()) {
            versioned = cache.put(key, value, lease);
            long oldLease = versioned != null ? versioned.getLease() : 0;
            if (oldLease > 0) {
                LeaseKeys lk = leaseMgr.get(oldLease);
                if (lk == null) {
                    throw new IllegalArgumentException("No lease with id " + oldLease);
                }
                lk.getKeys().remove(Bytes.wrap(key));
            }
            if (lease > 0) {
                LeaseKeys lk = leaseMgr.get(lease);
                if (lk == null) {
                    throw new IllegalArgumentException("No lease with id " + lease);
                }
                lk.getKeys().add(Bytes.wrap(key));
            }
        } else {
            versioned = cache.get(key);
        }
        PutResponse.Builder responseBuilder = PutResponse.newBuilder();
        responseBuilder.setHeader(toResponseHeader());
        if (request.getPrevKv() && versioned != null) {
            KeyValue kv = ProtoUtils.toKeyValue(key, versioned);
            responseBuilder.setPrevKv(kv);
        }
        return responseBuilder.build();
    }

    @Override
    public void deleteRange(DeleteRangeRequest request, StreamObserver<DeleteRangeResponse> responseObserver) {
        // TODO both key and range_end \0
        TransactionManager txMgr = KetaEngine.getInstance().getTxManager();
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
            throw new IllegalStateException(e);
        }
    }

    private DeleteRangeResponse doDeleteRange(DeleteRangeRequest request) {
        TxVersionedCache cache = KetaEngine.getInstance().getTxCache();
        byte[] from = request.getKey().toByteArray();
        byte[] to = request.getRangeEnd().toByteArray();
        List<byte[]> keys = new ArrayList<>();
        DeleteRangeResponse.Builder responseBuilder = DeleteRangeResponse.newBuilder();
        responseBuilder.setHeader(toResponseHeader());
        if (to.length > 0) {
            long count = 0L;
            try (KeyValueIterator<byte[], VersionedValue> iter = cache.range(from, true, to, false)) {
                while (iter.hasNext()) {
                    io.kcache.KeyValue<byte[], VersionedValue> entry = iter.next();
                    keys.add(entry.key);
                    if (request.getPrevKv()) {
                        KeyValue kv = ProtoUtils.toKeyValue(entry.key, entry.value);
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
                    KeyValue kv = ProtoUtils.toKeyValue(from, versioned);
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
        TransactionManager txMgr = KetaEngine.getInstance().getTxManager();
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
            throw new IllegalStateException(e);
        }
    }

    private TxnResponse doTxn(TxnRequest request) {
        boolean succeeded = doCompares(request.getCompareList());
        List<ResponseOp> responses = doRequests(succeeded ? request.getSuccessList() : request.getFailureList());
        return TxnResponse.newBuilder()
            .setHeader(toResponseHeader())
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
        TxVersionedCache cache = KetaEngine.getInstance().getTxCache();
        Compare.CompareTarget target = compare.getTarget();
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
        switch (compare.getTarget()) {
            case VERSION:
                return doCompareVersion(compare, versioned);
            case CREATE:
                return doCompareCreate(compare, versioned);
            case MOD:
                return doCompareRevision(compare, versioned);
            case VALUE:
                return doCompareValue(compare, versioned);
            default:
                throw new IllegalArgumentException("Unsupported target type " + compare.getTarget());
        }
    }

    private boolean doCompareVersion(Compare compare, VersionedValue versioned) {
        long cmpVersion = compare.getVersion();
        long version = versioned != null ? versioned.getSequence() : 0L;
        return doCompareLongs(compare, version, cmpVersion);
    }

    private boolean doCompareCreate(Compare compare, VersionedValue versioned) {
        long cmpCreate = compare.getCreateRevision();
        long create = versioned != null ? versioned.getCreate() : 0L;
        return doCompareLongs(compare, create, cmpCreate);
    }

    private boolean doCompareRevision(Compare compare, VersionedValue versioned) {
        long cmpMod = compare.getModRevision();
        long mod = versioned != null ? versioned.getCommit() : 0L;
        return doCompareLongs(compare, mod, cmpMod);
    }

    private boolean doCompareLongs(Compare compare, long value1, long value2) {
        int cmp = Long.compare(value1, value2);
        switch (compare.getResult()) {
            case EQUAL:
                return cmp == 0;
            case GREATER:
                return cmp > 0;
            case LESS:
                return cmp < 0;
            case NOT_EQUAL:
                return cmp != 0;
            default:
                throw new IllegalArgumentException("Unsupported compare type " + compare.getResult());
        }
    }
    private boolean doCompareValue(Compare compare, VersionedValue versioned) {
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
                throw new IllegalArgumentException("Unsupported compare type " + compare.getResult());
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
                throw new IllegalArgumentException("Unsupported request type " + request.getRequestCase());
        }
    }

    @Override
    public void compact(CompactionRequest request, StreamObserver<CompactionResponse> responseObserver) {
        super.compact(request, responseObserver);
    }

    private static ResponseHeader toResponseHeader() {
        KetaTransaction tx = KetaTransaction.currentTransaction();
        // TODO is this right?
        return ResponseHeader.newBuilder().setRevision(tx.getStartTimestamp()).build();
    }
}

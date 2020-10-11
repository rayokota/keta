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
import io.etcd.jetcd.api.DeleteRangeRequest;
import io.etcd.jetcd.api.DeleteRangeResponse;
import io.etcd.jetcd.api.KVGrpc;
import io.etcd.jetcd.api.KeyValue;
import io.etcd.jetcd.api.PutRequest;
import io.etcd.jetcd.api.PutResponse;
import io.etcd.jetcd.api.RangeRequest;
import io.etcd.jetcd.api.RangeResponse;
import io.etcd.jetcd.api.TxnRequest;
import io.etcd.jetcd.api.TxnResponse;
import io.grpc.stub.StreamObserver;
import io.kcache.KeyValueIterator;
import io.kcache.ketsie.KetsieEngine;
import io.kcache.ketsie.version.TxVersionedCache;
import io.kcache.ketsie.version.VersionedValue;
import org.apache.omid.transaction.RollbackException;
import org.apache.omid.transaction.Transaction;
import org.apache.omid.transaction.TransactionException;

public class KVImpl extends KVGrpc.KVImplBase {

    @Override
    public void range(RangeRequest request, StreamObserver<RangeResponse> responseObserver) {
        try {
            Transaction tx = KetsieEngine.getInstance().getTxManager().begin();
            TxVersionedCache cache = KetsieEngine.getInstance().getTxCache();
            byte[] from = request.getKey().toByteArray();
            byte[] to = request.getRangeEnd().toByteArray();
            RangeResponse.Builder responseBuilder = RangeResponse.newBuilder();
            if (to.length > 0) {
                // TODO handle to = new byte[]{0}
                KeyValueIterator<byte[], VersionedValue> iter = cache.range(from, true, to, false);
                while (iter.hasNext()) {
                    io.kcache.KeyValue<byte[], VersionedValue> elem = iter.next();
                    KeyValue kv = KeyValue.newBuilder()
                        .setKey(ByteString.copyFrom(elem.key))
                        .setValue(ByteString.copyFrom(elem.value.getValue()))
                        .build();
                    responseBuilder.addKvs(kv);
                }
            } else {
                VersionedValue elem = cache.get(from);
                KeyValue kv = KeyValue.newBuilder()
                    .setKey(ByteString.copyFrom(from))
                    .setValue(ByteString.copyFrom(elem.getValue()))
                    .build();
                responseBuilder.addKvs(kv);
            }
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
            KetsieEngine.getInstance().getTxManager().commit(tx);
        } catch (TransactionException | RollbackException e) {
            // TODO
        }
    }

    @Override
    public void put(PutRequest request, StreamObserver<PutResponse> responseObserver) {
        try {
            Transaction tx = KetsieEngine.getInstance().getTxManager().begin();
            TxVersionedCache cache = KetsieEngine.getInstance().getTxCache();
            cache.put(request.getKey().toByteArray(), request.getValue().toByteArray());
            ByteString bs = ByteString.copyFrom("hi".getBytes());
            KeyValue kv = KeyValue.newBuilder()
                .setKey(request.getKey())
                .setValue(request.getValue())
                .build();
            PutResponse putResponse = PutResponse.newBuilder().setPrevKv(kv).build();
            responseObserver.onNext(putResponse);
            responseObserver.onCompleted();
            KetsieEngine.getInstance().getTxManager().commit(tx);
        } catch (TransactionException | RollbackException e) {
            // TODO
        }
    }

    @Override
    public void deleteRange(DeleteRangeRequest request, StreamObserver<DeleteRangeResponse> responseObserver) {
        super.deleteRange(request, responseObserver);
    }

    @Override
    public void txn(TxnRequest request, StreamObserver<TxnResponse> responseObserver) {
        super.txn(request, responseObserver);
    }

    @Override
    public void compact(CompactionRequest request, StreamObserver<CompactionResponse> responseObserver) {
        super.compact(request, responseObserver);
    }
}

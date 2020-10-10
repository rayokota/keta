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
package io.kcache.ketsie.kv;

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

import java.io.IOException;

public class KVImpl extends KVGrpc.KVImplBase {
    @Override
    public void range(RangeRequest request, StreamObserver<RangeResponse> responseObserver) {
        super.range(request, responseObserver);
    }

    @Override
    public void put(PutRequest request, StreamObserver<PutResponse> responseObserver) {
        try {
            System.out.println("*** Put");
            ByteString.Output out = ByteString.newOutput();
            out.write("hi".getBytes());
            ByteString bs = out.toByteString();
            KeyValue kv = KeyValue.newBuilder().setKey(bs).setValue(bs).build();
            PutResponse putResponse = PutResponse.newBuilder().setPrevKv(kv).build();
            responseObserver.onNext(putResponse);
            responseObserver.onCompleted();
        } catch (IOException e) {
            super.put(request, responseObserver);

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

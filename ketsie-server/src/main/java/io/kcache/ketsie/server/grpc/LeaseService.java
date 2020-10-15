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

import io.etcd.jetcd.api.LeaseGrantRequest;
import io.etcd.jetcd.api.LeaseGrantResponse;
import io.etcd.jetcd.api.LeaseGrpc;
import io.etcd.jetcd.api.LeaseKeepAliveRequest;
import io.etcd.jetcd.api.LeaseKeepAliveResponse;
import io.etcd.jetcd.api.LeaseRevokeRequest;
import io.etcd.jetcd.api.LeaseRevokeResponse;
import io.etcd.jetcd.api.LeaseTimeToLiveRequest;
import io.etcd.jetcd.api.LeaseTimeToLiveResponse;
import io.grpc.stub.StreamObserver;

public class LeaseService extends LeaseGrpc.LeaseImplBase {

    @Override
    public void leaseGrant(LeaseGrantRequest request, StreamObserver<LeaseGrantResponse> responseObserver) {
        super.leaseGrant(request, responseObserver);
    }

    @Override
    public void leaseRevoke(LeaseRevokeRequest request, StreamObserver<LeaseRevokeResponse> responseObserver) {
        super.leaseRevoke(request, responseObserver);
    }

    @Override
    public StreamObserver<LeaseKeepAliveRequest> leaseKeepAlive(StreamObserver<LeaseKeepAliveResponse> responseObserver) {
        return super.leaseKeepAlive(responseObserver);
    }

    @Override
    public void leaseTimeToLive(LeaseTimeToLiveRequest request, StreamObserver<LeaseTimeToLiveResponse> responseObserver) {
        super.leaseTimeToLive(request, responseObserver);
    }
}

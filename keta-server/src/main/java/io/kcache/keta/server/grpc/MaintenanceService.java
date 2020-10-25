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

import io.etcd.jetcd.api.AlarmRequest;
import io.etcd.jetcd.api.AlarmResponse;
import io.etcd.jetcd.api.DefragmentRequest;
import io.etcd.jetcd.api.DefragmentResponse;
import io.etcd.jetcd.api.HashKVRequest;
import io.etcd.jetcd.api.HashKVResponse;
import io.etcd.jetcd.api.HashRequest;
import io.etcd.jetcd.api.HashResponse;
import io.etcd.jetcd.api.MaintenanceGrpc;
import io.etcd.jetcd.api.MoveLeaderRequest;
import io.etcd.jetcd.api.MoveLeaderResponse;
import io.etcd.jetcd.api.SnapshotRequest;
import io.etcd.jetcd.api.SnapshotResponse;
import io.etcd.jetcd.api.StatusRequest;
import io.etcd.jetcd.api.StatusResponse;
import io.grpc.stub.StreamObserver;
import io.kcache.keta.server.grpc.utils.GrpcUtils;
import io.kcache.keta.server.leader.KetaLeaderElector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MaintenanceService extends MaintenanceGrpc.MaintenanceImplBase {
    private final static Logger LOG = LoggerFactory.getLogger(MaintenanceService.class);

    private final KetaLeaderElector elector;

    public MaintenanceService(KetaLeaderElector elector) {
        this.elector = elector;
    }

    @Override
    public void alarm(AlarmRequest request, StreamObserver<AlarmResponse> responseObserver) {
        super.alarm(request, responseObserver);
    }

    @Override
    public void status(StatusRequest request, StreamObserver<StatusResponse> responseObserver) {
        StatusResponse.Builder responseBuilder = StatusResponse.newBuilder();
        responseBuilder.setHeader(GrpcUtils.toResponseHeader(elector.getMemberId()));
        responseBuilder.setLeader(elector.getLeaderId());
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void defragment(DefragmentRequest request, StreamObserver<DefragmentResponse> responseObserver) {
        super.defragment(request, responseObserver);
    }

    @Override
    public void hash(HashRequest request, StreamObserver<HashResponse> responseObserver) {
        super.hash(request, responseObserver);
    }

    @Override
    public void hashKV(HashKVRequest request, StreamObserver<HashKVResponse> responseObserver) {
        super.hashKV(request, responseObserver);
    }

    @Override
    public void snapshot(SnapshotRequest request, StreamObserver<SnapshotResponse> responseObserver) {
        super.snapshot(request, responseObserver);
    }

    @Override
    public void moveLeader(MoveLeaderRequest request, StreamObserver<MoveLeaderResponse> responseObserver) {
        super.moveLeader(request, responseObserver);
    }
}

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

import com.google.protobuf.ByteString;
import io.etcd.jetcd.api.LeaseGrantRequest;
import io.etcd.jetcd.api.LeaseGrantResponse;
import io.etcd.jetcd.api.LeaseGrpc;
import io.etcd.jetcd.api.LeaseKeepAliveRequest;
import io.etcd.jetcd.api.LeaseKeepAliveResponse;
import io.etcd.jetcd.api.LeaseRevokeRequest;
import io.etcd.jetcd.api.LeaseRevokeResponse;
import io.etcd.jetcd.api.LeaseTimeToLiveRequest;
import io.etcd.jetcd.api.LeaseTimeToLiveResponse;
import io.etcd.jetcd.api.ResponseHeader;
import io.grpc.stub.StreamObserver;
import io.kcache.keta.KetaEngine;
import io.kcache.keta.lease.KetaLeaseManager;
import io.kcache.keta.lease.Lease;
import io.kcache.keta.lease.LeaseKeys;
import io.kcache.keta.server.grpc.utils.GrpcUtils;
import io.kcache.keta.server.grpc.errors.KetaErrorType;
import io.kcache.keta.server.leader.KetaLeaderElector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;

public class LeaseService extends LeaseGrpc.LeaseImplBase {
    private final static Logger LOG = LoggerFactory.getLogger(LeaseService.class);

    private final KetaLeaderElector elector;

    public LeaseService(KetaLeaderElector elector) {
        this.elector = elector;
    }

    @Override
    public void leaseGrant(LeaseGrantRequest request, StreamObserver<LeaseGrantResponse> responseObserver) {
        if (!elector.isLeader()) {
            responseObserver.onError((KetaErrorType.LeaderChanged.toException()));
            return;
        }
        Lease lease = new Lease(request.getID(), request.getTTL(), System.currentTimeMillis() + request.getTTL() * 1000);
        KetaLeaseManager leaseMgr = KetaEngine.getInstance().getLeaseManager();
        try {
            LeaseKeys lk = leaseMgr.grant(lease);
            responseObserver.onNext(LeaseGrantResponse.newBuilder()
                .setHeader(GrpcUtils.toResponseHeader(elector.getMemberId()))
                .setID(lk.getLease().getId())
                .setTTL(lk.getLease().getTtl())
                .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(GrpcUtils.toStatusException(e));
        }
    }

    @Override
    public void leaseRevoke(LeaseRevokeRequest request, StreamObserver<LeaseRevokeResponse> responseObserver) {
        if (!elector.isLeader()) {
            responseObserver.onError((KetaErrorType.LeaderChanged.toException()));
            return;
        }
        long id = request.getID();
        if (id == 0) {
            responseObserver.onError(KetaErrorType.LeaseNotFound.toException());
            return;
        }
        KetaLeaseManager leaseMgr = KetaEngine.getInstance().getLeaseManager();
        try {
            leaseMgr.revoke(id);
            responseObserver.onNext(LeaseRevokeResponse.newBuilder()
                .setHeader(GrpcUtils.toResponseHeader(elector.getMemberId()))
                .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(GrpcUtils.toStatusException(e));
        }
    }

    @Override
    public StreamObserver<LeaseKeepAliveRequest> leaseKeepAlive(StreamObserver<LeaseKeepAliveResponse> responseObserver) {
        return new StreamObserver<LeaseKeepAliveRequest>() {
            @Override
            public void onNext(LeaseKeepAliveRequest value) {
                if (!elector.isLeader()) {
                    responseObserver.onError((KetaErrorType.LeaderChanged.toException()));
                    return;
                }
                long id = value.getID();
                KetaLeaseManager leaseMgr = KetaEngine.getInstance().getLeaseManager();
                try {
                    LeaseKeys lease = leaseMgr.renew(id);
                    responseObserver.onNext(LeaseKeepAliveResponse.newBuilder()
                        .setHeader(GrpcUtils.toResponseHeader(elector.getMemberId()))
                        .setID(id).setTTL(lease.getTtl()).build());
                } catch (Exception e) {
                    responseObserver.onError(GrpcUtils.toStatusException(e));
                }
            }

            @Override
            public void onError(Throwable t) {
                LOG.error(t.getMessage());
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public void leaseTimeToLive(LeaseTimeToLiveRequest request, StreamObserver<LeaseTimeToLiveResponse> responseObserver) {
        if (!elector.isLeader()) {
            responseObserver.onError((KetaErrorType.LeaderChanged.toException()));
            return;
        }
        long id = request.getID();
        KetaLeaseManager leaseMgr = KetaEngine.getInstance().getLeaseManager();
        try {
            LeaseKeys lease = leaseMgr.get(id);
            LeaseTimeToLiveResponse.Builder builder = LeaseTimeToLiveResponse.newBuilder()
                .setHeader(GrpcUtils.toResponseHeader(elector.getMemberId()))
                .setID(id)
                .setTTL((lease.getExpiry() - System.currentTimeMillis()) / 1000)
                .setGrantedTTL(lease.getTtl());
            if (request.getKeys()) {
                builder.addAllKeys(lease.getKeys().stream()
                    .map(k -> ByteString.copyFrom(k.get()))
                    .collect(Collectors.toList()));
            }
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(GrpcUtils.toStatusException(e));
        }
    }
}

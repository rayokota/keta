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
import io.etcd.jetcd.api.LeaseLeasesRequest;
import io.etcd.jetcd.api.LeaseLeasesResponse;
import io.etcd.jetcd.api.LeaseRevokeRequest;
import io.etcd.jetcd.api.LeaseRevokeResponse;
import io.etcd.jetcd.api.LeaseStatus;
import io.etcd.jetcd.api.LeaseTimeToLiveRequest;
import io.etcd.jetcd.api.LeaseTimeToLiveResponse;
import io.grpc.stub.StreamObserver;
import io.kcache.keta.KetaEngine;
import io.kcache.keta.auth.KetaAuthManager;
import io.kcache.keta.lease.KetaLeaseManager;
import io.kcache.keta.lease.LeaseKeys;
import io.kcache.keta.pb.Lease;
import io.kcache.keta.server.grpc.utils.AuthServerInterceptor;
import io.kcache.keta.server.grpc.utils.GrpcUtils;
import io.kcache.keta.server.grpc.errors.KetaErrorType;
import io.kcache.keta.server.leader.KetaLeaderElector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class LeaseService extends LeaseGrpc.LeaseImplBase {
    private final static Logger LOG = LoggerFactory.getLogger(LeaseService.class);

    private final KetaLeaderElector elector;

    public LeaseService(KetaLeaderElector elector) {
        this.elector = elector;
    }

    @Override
    public void leaseGrant(LeaseGrantRequest request, StreamObserver<LeaseGrantResponse> responseObserver) {
        if (!KetaEngine.getInstance().isLeader()) {
            responseObserver.onError((KetaErrorType.LeaderChanged.toException()));
            return;
        }
        long id = request.getID();
        LOG.info("Lease grant request: {}", id);
        Lease lease = Lease.newBuilder()
            .setID(id)
            .setTTL(request.getTTL())
            .setExpiry(System.currentTimeMillis() + request.getTTL() * 1000)
            .build();
        KetaLeaseManager leaseMgr = KetaEngine.getInstance().getLeaseManager();
        try {
            LeaseKeys lk = leaseMgr.grant(lease);
            responseObserver.onNext(LeaseGrantResponse.newBuilder()
                .setHeader(GrpcUtils.toResponseHeader(elector.getMemberId()))
                .setID(lk.getLease().getID())
                .setTTL(lk.getLease().getTTL())
                .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(GrpcUtils.toStatusException(e));
        }
    }

    @Override
    public void leaseRevoke(LeaseRevokeRequest request, StreamObserver<LeaseRevokeResponse> responseObserver) {
        if (!KetaEngine.getInstance().isLeader()) {
            responseObserver.onError((KetaErrorType.LeaderChanged.toException()));
            return;
        }
        long id = request.getID();
        checkLeasePuts(id);
        LOG.info("Lease revoke request: {}", id);
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

    private void checkLeasePuts(long lease) {
        if (lease == 0) {
            return;
        }
        KetaLeaseManager leaseMgr = KetaEngine.getInstance().getLeaseManager();
        LeaseKeys lk = leaseMgr.get(lease);
        KetaAuthManager authMgr = KetaEngine.getInstance().getAuthManager();
        if (lk != null) {
            for (ByteString key : lk.getKeys()) {
                authMgr.checkPutPermitted(AuthServerInterceptor.USER_CTX_KEY.get(), key);
            }
        }
    }

    @Override
    public StreamObserver<LeaseKeepAliveRequest> leaseKeepAlive(StreamObserver<LeaseKeepAliveResponse> responseObserver) {
        return new StreamObserver<LeaseKeepAliveRequest>() {
            @Override
            public void onNext(LeaseKeepAliveRequest value) {
                if (!KetaEngine.getInstance().isLeader()) {
                    responseObserver.onError((KetaErrorType.LeaderChanged.toException()));
                    return;
                }
                long id = value.getID();
                LOG.info("Lease keep alive request: {}", id);
                KetaLeaseManager leaseMgr = KetaEngine.getInstance().getLeaseManager();
                try {
                    LeaseKeys lease = leaseMgr.renew(id);
                    responseObserver.onNext(LeaseKeepAliveResponse.newBuilder()
                        .setHeader(GrpcUtils.toResponseHeader(elector.getMemberId()))
                        .setID(id).setTTL(lease.getTTL()).build());
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
        if (!KetaEngine.getInstance().isLeader()) {
            responseObserver.onError((KetaErrorType.LeaderChanged.toException()));
            return;
        }
        long id = request.getID();
        LOG.info("Lease time to live request: {}", id);
        KetaLeaseManager leaseMgr = KetaEngine.getInstance().getLeaseManager();
        try {
            LeaseKeys lease = leaseMgr.get(id);
            LeaseTimeToLiveResponse.Builder builder = LeaseTimeToLiveResponse.newBuilder()
                .setHeader(GrpcUtils.toResponseHeader(elector.getMemberId()))
                .setID(id)
                .setTTL((lease.getExpiry() - System.currentTimeMillis()) / 1000)
                .setGrantedTTL(lease.getTTL());
            if (request.getKeys()) {
                builder.addAllKeys(lease.getKeys());
            }
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(GrpcUtils.toStatusException(e));
        }
    }

    @Override
    public void leaseLeases(LeaseLeasesRequest request, StreamObserver<LeaseLeasesResponse> responseObserver) {
        if (!KetaEngine.getInstance().isLeader()) {
            responseObserver.onError((KetaErrorType.LeaderChanged.toException()));
            return;
        }
        LOG.info("Lease leases request");
        KetaLeaseManager leaseMgr = KetaEngine.getInstance().getLeaseManager();
        try {
            List<LeaseStatus> leases = leaseMgr.leaseIds().stream()
                .map(l -> LeaseStatus.newBuilder()
                    .setID(l)
                    .build())
                .collect(Collectors.toList());
            responseObserver.onNext(LeaseLeasesResponse.newBuilder()
                .addAllLeases(leases)
                .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(GrpcUtils.toStatusException(e));
        }
    }
}

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
import io.grpc.stub.StreamObserver;
import io.kcache.keta.KetaEngine;
import io.kcache.keta.lease.KetaLeaseManager;
import io.kcache.keta.lease.Lease;
import io.kcache.keta.lease.LeaseKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;

public class LeaseService extends LeaseGrpc.LeaseImplBase {
    private final static Logger LOG = LoggerFactory.getLogger(LeaseService.class);

    @Override
    public void leaseGrant(LeaseGrantRequest request, StreamObserver<LeaseGrantResponse> responseObserver) {
        Lease lease = new Lease(request.getID(), request.getTTL(), System.currentTimeMillis() + request.getTTL() * 1000);
        KetaLeaseManager leaseMgr = KetaEngine.getInstance().getLeaseManager();
        LeaseKeys lk = leaseMgr.grant(lease);
        responseObserver.onNext(LeaseGrantResponse.newBuilder()
            .setID(lk.getLease().getId())
            .setTTL(lk.getLease().getTtl())
            .build());
        responseObserver.onCompleted();
    }

    @Override
    public void leaseRevoke(LeaseRevokeRequest request, StreamObserver<LeaseRevokeResponse> responseObserver) {
        long id = request.getID();
        if (id == 0) {
            throw new IllegalArgumentException("No lease id");
        }
        KetaLeaseManager leaseMgr = KetaEngine.getInstance().getLeaseManager();
        leaseMgr.revoke(id);
        responseObserver.onNext(LeaseRevokeResponse.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<LeaseKeepAliveRequest> leaseKeepAlive(StreamObserver<LeaseKeepAliveResponse> responseObserver) {
        return new StreamObserver<LeaseKeepAliveRequest>() {
            @Override
            public void onNext(LeaseKeepAliveRequest value) {
                long id = value.getID();
                KetaLeaseManager leaseMgr = KetaEngine.getInstance().getLeaseManager();
                LeaseKeys lease = leaseMgr.renew(id);
                responseObserver.onNext(LeaseKeepAliveResponse.newBuilder().setID(id).setTTL(lease.getTtl()).build());
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
        long id = request.getID();
        KetaLeaseManager leaseMgr = KetaEngine.getInstance().getLeaseManager();
        LeaseKeys lease = leaseMgr.get(id);
        responseObserver.onNext(LeaseTimeToLiveResponse.newBuilder()
            .setID(id)
            .setTTL(lease.getTtl())
            .addAllKeys(lease.getKeys().stream().map(k -> ByteString.copyFrom(k.get())).collect(Collectors.toList()))
            .setGrantedTTL(lease.getTtl())
            .build());
        responseObserver.onCompleted();
    }
}

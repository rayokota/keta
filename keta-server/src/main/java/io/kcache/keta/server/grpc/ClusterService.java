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

import io.etcd.jetcd.api.ClusterGrpc;
import io.etcd.jetcd.api.Member;
import io.etcd.jetcd.api.MemberAddRequest;
import io.etcd.jetcd.api.MemberAddResponse;
import io.etcd.jetcd.api.MemberListRequest;
import io.etcd.jetcd.api.MemberListResponse;
import io.etcd.jetcd.api.MemberRemoveRequest;
import io.etcd.jetcd.api.MemberRemoveResponse;
import io.etcd.jetcd.api.MemberUpdateRequest;
import io.etcd.jetcd.api.MemberUpdateResponse;
import io.etcd.jetcd.api.ResponseHeader;
import io.grpc.stub.StreamObserver;
import io.kcache.keta.server.grpc.errors.KetaErrorType;
import io.kcache.keta.server.grpc.utils.GrpcUtils;
import io.kcache.keta.server.leader.KetaIdentity;
import io.kcache.keta.server.leader.KetaLeaderElector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

public class ClusterService extends ClusterGrpc.ClusterImplBase {
    private final static Logger LOG = LoggerFactory.getLogger(ClusterService.class);

    private final KetaLeaderElector elector;

    public ClusterService(KetaLeaderElector elector) {
        this.elector = elector;
    }

    @Override
    public void memberAdd(MemberAddRequest request, StreamObserver<MemberAddResponse> responseObserver) {
        super.memberAdd(request, responseObserver);
    }

    @Override
    public void memberRemove(MemberRemoveRequest request, StreamObserver<MemberRemoveResponse> responseObserver) {
        super.memberRemove(request, responseObserver);
    }

    @Override
    public void memberUpdate(MemberUpdateRequest request, StreamObserver<MemberUpdateResponse> responseObserver) {
        super.memberUpdate(request, responseObserver);
    }

    @Override
    public void memberList(MemberListRequest request, StreamObserver<MemberListResponse> responseObserver) {
        MemberListResponse.Builder responseBuilder = MemberListResponse.newBuilder();
        responseBuilder.setHeader(GrpcUtils.toResponseHeader(elector.getMemberId()));
        Collection<KetaIdentity> members = elector.getMembers();
        if (members.isEmpty()) {
            responseObserver.onError((KetaErrorType.NoLeader.toException()));
            return;
        }
        for (KetaIdentity member : members) {
            responseBuilder.addMembers(Member.newBuilder()
                .setID(elector.getMemberId())
                .setName(member.getHost())
                .build()
            );
        }
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }
}

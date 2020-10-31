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

import io.etcd.jetcd.api.AuthDisableRequest;
import io.etcd.jetcd.api.AuthDisableResponse;
import io.etcd.jetcd.api.AuthEnableRequest;
import io.etcd.jetcd.api.AuthEnableResponse;
import io.etcd.jetcd.api.AuthGrpc;
import io.etcd.jetcd.api.AuthRoleAddRequest;
import io.etcd.jetcd.api.AuthRoleAddResponse;
import io.etcd.jetcd.api.AuthRoleDeleteRequest;
import io.etcd.jetcd.api.AuthRoleDeleteResponse;
import io.etcd.jetcd.api.AuthRoleGetRequest;
import io.etcd.jetcd.api.AuthRoleGetResponse;
import io.etcd.jetcd.api.AuthRoleGrantPermissionRequest;
import io.etcd.jetcd.api.AuthRoleGrantPermissionResponse;
import io.etcd.jetcd.api.AuthRoleListRequest;
import io.etcd.jetcd.api.AuthRoleListResponse;
import io.etcd.jetcd.api.AuthRoleRevokePermissionRequest;
import io.etcd.jetcd.api.AuthRoleRevokePermissionResponse;
import io.etcd.jetcd.api.AuthUserAddRequest;
import io.etcd.jetcd.api.AuthUserAddResponse;
import io.etcd.jetcd.api.AuthUserChangePasswordRequest;
import io.etcd.jetcd.api.AuthUserChangePasswordResponse;
import io.etcd.jetcd.api.AuthUserDeleteRequest;
import io.etcd.jetcd.api.AuthUserDeleteResponse;
import io.etcd.jetcd.api.AuthUserGetRequest;
import io.etcd.jetcd.api.AuthUserGetResponse;
import io.etcd.jetcd.api.AuthUserGrantRoleRequest;
import io.etcd.jetcd.api.AuthUserGrantRoleResponse;
import io.etcd.jetcd.api.AuthUserListRequest;
import io.etcd.jetcd.api.AuthUserListResponse;
import io.etcd.jetcd.api.AuthUserRevokeRoleRequest;
import io.etcd.jetcd.api.AuthUserRevokeRoleResponse;
import io.etcd.jetcd.api.AuthenticateRequest;
import io.etcd.jetcd.api.AuthenticateResponse;
import io.grpc.stub.StreamObserver;
import io.kcache.keta.auth.TokenProvider;
import io.kcache.keta.server.grpc.utils.GrpcUtils;
import io.kcache.keta.server.leader.KetaLeaderElector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthService extends AuthGrpc.AuthImplBase {
    private final static Logger LOG = LoggerFactory.getLogger(AuthService.class);

    private final KetaLeaderElector elector;
    private final TokenProvider tokenProvider;

    public AuthService(KetaLeaderElector elector, TokenProvider tokenProvider) {
        this.elector = elector;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public void authEnable(AuthEnableRequest request, StreamObserver<AuthEnableResponse> responseObserver) {
        super.authEnable(request, responseObserver);
    }

    @Override
    public void authDisable(AuthDisableRequest request, StreamObserver<AuthDisableResponse> responseObserver) {
        super.authDisable(request, responseObserver);
    }

    @Override
    public void authenticate(AuthenticateRequest request, StreamObserver<AuthenticateResponse> responseObserver) {
        String user = request.getName();
        System.out.println("*** got user " + user);
        try {
            responseObserver.onNext(AuthenticateResponse.newBuilder()
                .setHeader(GrpcUtils.toResponseHeader(elector.getMemberId()))
                .setToken(tokenProvider.assignToken(user))
                .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(GrpcUtils.toStatusException(e));
        }
    }

    @Override
    public void userAdd(AuthUserAddRequest request, StreamObserver<AuthUserAddResponse> responseObserver) {
        super.userAdd(request, responseObserver);
    }

    @Override
    public void userGet(AuthUserGetRequest request, StreamObserver<AuthUserGetResponse> responseObserver) {
        super.userGet(request, responseObserver);
    }

    @Override
    public void userList(AuthUserListRequest request, StreamObserver<AuthUserListResponse> responseObserver) {
        super.userList(request, responseObserver);
    }

    @Override
    public void userDelete(AuthUserDeleteRequest request, StreamObserver<AuthUserDeleteResponse> responseObserver) {
        super.userDelete(request, responseObserver);
    }

    @Override
    public void userChangePassword(AuthUserChangePasswordRequest request, StreamObserver<AuthUserChangePasswordResponse> responseObserver) {
        super.userChangePassword(request, responseObserver);
    }

    @Override
    public void userGrantRole(AuthUserGrantRoleRequest request, StreamObserver<AuthUserGrantRoleResponse> responseObserver) {
        super.userGrantRole(request, responseObserver);
    }

    @Override
    public void userRevokeRole(AuthUserRevokeRoleRequest request, StreamObserver<AuthUserRevokeRoleResponse> responseObserver) {
        super.userRevokeRole(request, responseObserver);
    }

    @Override
    public void roleAdd(AuthRoleAddRequest request, StreamObserver<AuthRoleAddResponse> responseObserver) {
        super.roleAdd(request, responseObserver);
    }

    @Override
    public void roleGet(AuthRoleGetRequest request, StreamObserver<AuthRoleGetResponse> responseObserver) {
        super.roleGet(request, responseObserver);
    }

    @Override
    public void roleList(AuthRoleListRequest request, StreamObserver<AuthRoleListResponse> responseObserver) {
        super.roleList(request, responseObserver);
    }

    @Override
    public void roleDelete(AuthRoleDeleteRequest request, StreamObserver<AuthRoleDeleteResponse> responseObserver) {
        super.roleDelete(request, responseObserver);
    }

    @Override
    public void roleGrantPermission(AuthRoleGrantPermissionRequest request, StreamObserver<AuthRoleGrantPermissionResponse> responseObserver) {
        super.roleGrantPermission(request, responseObserver);
    }

    @Override
    public void roleRevokePermission(AuthRoleRevokePermissionRequest request, StreamObserver<AuthRoleRevokePermissionResponse> responseObserver) {
        super.roleRevokePermission(request, responseObserver);
    }
}

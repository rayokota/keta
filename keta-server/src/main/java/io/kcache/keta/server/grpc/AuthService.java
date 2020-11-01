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
import io.etcd.jetcd.api.Role;
import io.etcd.jetcd.api.User;
import io.grpc.stub.StreamObserver;
import io.kcache.keta.KetaEngine;
import io.kcache.keta.auth.KetaAuthManager;
import io.kcache.keta.server.grpc.utils.GrpcUtils;
import io.kcache.keta.server.leader.KetaLeaderElector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class AuthService extends AuthGrpc.AuthImplBase {
    private final static Logger LOG = LoggerFactory.getLogger(AuthService.class);

    private final KetaLeaderElector elector;

    public AuthService(KetaLeaderElector elector) {
        this.elector = elector;
    }

    @Override
    public void authEnable(AuthEnableRequest request, StreamObserver<AuthEnableResponse> responseObserver) {
        KetaAuthManager authMgr = KetaEngine.getInstance().getAuthManager();
        try {
            authMgr.enableAuth();
            responseObserver.onNext(AuthEnableResponse.newBuilder()
                .setHeader(GrpcUtils.toResponseHeader(elector.getMemberId()))
                .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(GrpcUtils.toStatusException(e));
        }
    }

    @Override
    public void authDisable(AuthDisableRequest request, StreamObserver<AuthDisableResponse> responseObserver) {
        KetaAuthManager authMgr = KetaEngine.getInstance().getAuthManager();
        try {
            authMgr.disableAuth();
            responseObserver.onNext(AuthDisableResponse.newBuilder()
                .setHeader(GrpcUtils.toResponseHeader(elector.getMemberId()))
                .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(GrpcUtils.toStatusException(e));
        }
    }

    @Override
    public void authenticate(AuthenticateRequest request, StreamObserver<AuthenticateResponse> responseObserver) {
        KetaAuthManager authMgr = KetaEngine.getInstance().getAuthManager();
        try {
            String token = authMgr.authenticate(request.getName(), request.getPassword());
            responseObserver.onNext(AuthenticateResponse.newBuilder()
                .setHeader(GrpcUtils.toResponseHeader(elector.getMemberId()))
                .setToken(token)
                .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(GrpcUtils.toStatusException(e));
        }
    }

    @Override
    public void userAdd(AuthUserAddRequest request, StreamObserver<AuthUserAddResponse> responseObserver) {
        KetaAuthManager authMgr = KetaEngine.getInstance().getAuthManager();
        try {
            authMgr.addUser(request.getName(), request.getPassword());
            responseObserver.onNext(AuthUserAddResponse.newBuilder()
                .setHeader(GrpcUtils.toResponseHeader(elector.getMemberId()))
                .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(GrpcUtils.toStatusException(e));
        }
    }

    @Override
    public void userGet(AuthUserGetRequest request, StreamObserver<AuthUserGetResponse> responseObserver) {
        KetaAuthManager authMgr = KetaEngine.getInstance().getAuthManager();
        try {
            User user = authMgr.getUser(request.getName());
            responseObserver.onNext(AuthUserGetResponse.newBuilder()
                .setHeader(GrpcUtils.toResponseHeader(elector.getMemberId()))
                .addAllRoles(user.getRolesList())
                .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(GrpcUtils.toStatusException(e));
        }
    }

    @Override
    public void userList(AuthUserListRequest request, StreamObserver<AuthUserListResponse> responseObserver) {
        KetaAuthManager authMgr = KetaEngine.getInstance().getAuthManager();
        try {
            Set<String> users = authMgr.listUsers();
            responseObserver.onNext(AuthUserListResponse.newBuilder()
                .setHeader(GrpcUtils.toResponseHeader(elector.getMemberId()))
                .addAllUsers(users)
                .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(GrpcUtils.toStatusException(e));
        }
    }

    @Override
    public void userDelete(AuthUserDeleteRequest request, StreamObserver<AuthUserDeleteResponse> responseObserver) {
        KetaAuthManager authMgr = KetaEngine.getInstance().getAuthManager();
        try {
            authMgr.deleteUser(request.getName());
            responseObserver.onNext(AuthUserDeleteResponse.newBuilder()
                .setHeader(GrpcUtils.toResponseHeader(elector.getMemberId()))
                .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(GrpcUtils.toStatusException(e));
        }
    }

    @Override
    public void userChangePassword(AuthUserChangePasswordRequest request, StreamObserver<AuthUserChangePasswordResponse> responseObserver) {
        KetaAuthManager authMgr = KetaEngine.getInstance().getAuthManager();
        try {
            authMgr.changePassword(request.getName(), request.getPassword());
            responseObserver.onNext(AuthUserChangePasswordResponse.newBuilder()
                .setHeader(GrpcUtils.toResponseHeader(elector.getMemberId()))
                .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(GrpcUtils.toStatusException(e));
        }
    }

    @Override
    public void userGrantRole(AuthUserGrantRoleRequest request, StreamObserver<AuthUserGrantRoleResponse> responseObserver) {
        KetaAuthManager authMgr = KetaEngine.getInstance().getAuthManager();
        try {
            authMgr.grantRole(request.getUser(), request.getRole());
            responseObserver.onNext(AuthUserGrantRoleResponse.newBuilder()
                .setHeader(GrpcUtils.toResponseHeader(elector.getMemberId()))
                .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(GrpcUtils.toStatusException(e));
        }
    }

    @Override
    public void userRevokeRole(AuthUserRevokeRoleRequest request, StreamObserver<AuthUserRevokeRoleResponse> responseObserver) {
        KetaAuthManager authMgr = KetaEngine.getInstance().getAuthManager();
        try {
            authMgr.revokeRole(request.getName(), request.getRole());
            responseObserver.onNext(AuthUserRevokeRoleResponse.newBuilder()
                .setHeader(GrpcUtils.toResponseHeader(elector.getMemberId()))
                .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(GrpcUtils.toStatusException(e));
        }
    }

    @Override
    public void roleAdd(AuthRoleAddRequest request, StreamObserver<AuthRoleAddResponse> responseObserver) {
        KetaAuthManager authMgr = KetaEngine.getInstance().getAuthManager();
        try {
            authMgr.addRole(request.getName());
            responseObserver.onNext(AuthRoleAddResponse.newBuilder()
                .setHeader(GrpcUtils.toResponseHeader(elector.getMemberId()))
                .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(GrpcUtils.toStatusException(e));
        }
    }

    @Override
    public void roleGet(AuthRoleGetRequest request, StreamObserver<AuthRoleGetResponse> responseObserver) {
        KetaAuthManager authMgr = KetaEngine.getInstance().getAuthManager();
        try {
            Role role = authMgr.getRole(request.getRole());
            responseObserver.onNext(AuthRoleGetResponse.newBuilder()
                .setHeader(GrpcUtils.toResponseHeader(elector.getMemberId()))
                .addAllPerm(role.getKeyPermissionList())
                .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(GrpcUtils.toStatusException(e));
        }
    }

    @Override
    public void roleList(AuthRoleListRequest request, StreamObserver<AuthRoleListResponse> responseObserver) {
        KetaAuthManager authMgr = KetaEngine.getInstance().getAuthManager();
        try {
            Set<String> roles = authMgr.listRoles();
            responseObserver.onNext(AuthRoleListResponse.newBuilder()
                .setHeader(GrpcUtils.toResponseHeader(elector.getMemberId()))
                .addAllRoles(roles)
                .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(GrpcUtils.toStatusException(e));
        }
    }

    @Override
    public void roleDelete(AuthRoleDeleteRequest request, StreamObserver<AuthRoleDeleteResponse> responseObserver) {
        KetaAuthManager authMgr = KetaEngine.getInstance().getAuthManager();
        try {
            authMgr.deleteRole(request.getRole());
            responseObserver.onNext(AuthRoleDeleteResponse.newBuilder()
                .setHeader(GrpcUtils.toResponseHeader(elector.getMemberId()))
                .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(GrpcUtils.toStatusException(e));
        }
    }

    @Override
    public void roleGrantPermission(AuthRoleGrantPermissionRequest request, StreamObserver<AuthRoleGrantPermissionResponse> responseObserver) {
        KetaAuthManager authMgr = KetaEngine.getInstance().getAuthManager();
        try {
            authMgr.grantPermission(request.getName(), request.getPerm());
            responseObserver.onNext(AuthRoleGrantPermissionResponse.newBuilder()
                .setHeader(GrpcUtils.toResponseHeader(elector.getMemberId()))
                .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(GrpcUtils.toStatusException(e));
        }
    }

    @Override
    public void roleRevokePermission(AuthRoleRevokePermissionRequest request, StreamObserver<AuthRoleRevokePermissionResponse> responseObserver) {
        KetaAuthManager authMgr = KetaEngine.getInstance().getAuthManager();
        try {
            authMgr.revokePermission(request.getRole(), request.getKeyBytes(), request.getRangeEndBytes());
            responseObserver.onNext(AuthRoleRevokePermissionResponse.newBuilder()
                .setHeader(GrpcUtils.toResponseHeader(elector.getMemberId()))
                .build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(GrpcUtils.toStatusException(e));
        }
    }
}

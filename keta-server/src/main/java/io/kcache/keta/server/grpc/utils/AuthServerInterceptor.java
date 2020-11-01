/*
 * Copyright 2016 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.kcache.keta.server.grpc.utils;

import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.kcache.keta.KetaEngine;
import io.kcache.keta.auth.KetaAuthManager;
import io.kcache.keta.server.grpc.errors.KetaErrorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthServerInterceptor implements ServerInterceptor {
    private static final Logger LOG = LoggerFactory.getLogger(AuthServerInterceptor.class);

    private static final ServerCall.Listener NOOP_LISTENER = new ServerCall.Listener() {
    };

    public static final Metadata.Key<String> TOKEN = Metadata.Key.of("token", Metadata.ASCII_STRING_MARSHALLER);
    public static final Context.Key<String> TOKEN_CTX_KEY = Context.key("token");
    public static final Context.Key<String> USER_CTX_KEY = Context.key("username");

    public AuthServerInterceptor() {
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {
        KetaAuthManager authMgr = KetaEngine.getInstance().getAuthManager();
        String methodName = serverCall.getMethodDescriptor().getFullMethodName();
        Context ctx = Context.current();
        if (authMgr.isAuthEnabled() && !methodName.equals("etcdserverpb.Auth/Authenticate")) {
            String jwt = metadata.get(TOKEN);
            if (jwt == null) {
                serverCall.close(KetaErrorType.UserEmpty.toStatus(), metadata);
                return NOOP_LISTENER;
            }

            try {
                String user = authMgr.getUserFromToken(jwt);
                ctx = ctx.withValue(USER_CTX_KEY, user).withValue(TOKEN_CTX_KEY, jwt);
            } catch (Exception e) {
                LOG.error("Verification failed - Unauthenticated!");
                serverCall.close(KetaErrorType.AuthFailed.toStatus(), metadata);
                return NOOP_LISTENER;
            }
        }
        return Contexts.interceptCall(ctx, serverCall, metadata, serverCallHandler);
    }
}

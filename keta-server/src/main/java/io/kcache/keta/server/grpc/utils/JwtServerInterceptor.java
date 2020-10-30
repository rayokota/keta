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

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;

import java.util.Map;

public class JwtServerInterceptor implements ServerInterceptor {
    private static final ServerCall.Listener NOOP_LISTENER = new ServerCall.Listener() {
    };

    private final String secret;
    private final JWTVerifier verifier;

    public JwtServerInterceptor(String secret) {
        this.secret = secret;
        this.verifier = null;//JWT.create().with
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {
      /*
    String jwt = metadata.get(Constant.JWT_METADATA_KEY);
    if (jwt == null) {
      serverCall.close(Status.UNAUTHENTICATED.withDescription("JWT Token is missing from Metadata"), metadata);
      return NOOP_LISTENER;
    }

    Context ctx;
    try {
      Map<String, Object> verified = verifier.verify(jwt);
      ctx = Context.current().withValue(Constant.USER_ID_CTX_KEY, verified.getOrDefault("sub", "anonymous").toString())
          .withValue(Constant.JWT_CTX_KEY, jwt);
    } catch (Exception e) {
      System.out.println("Verification failed - Unauthenticated!");
      serverCall.close(Status.UNAUTHENTICATED.withDescription(e.getMessage()).withCause(e), metadata);
      return NOOP_LISTENER;
    }

       */
        Context ctx = Context.current();
        System.out.println("*** intercepted " + serverCall);
        return Contexts.interceptCall(ctx, serverCall, metadata, serverCallHandler);

    }
}

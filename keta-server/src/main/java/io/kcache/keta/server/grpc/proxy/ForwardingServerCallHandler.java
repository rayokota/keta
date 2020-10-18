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
package io.kcache.keta.server.grpc.proxy;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;

import java.io.InputStream;

public final class ForwardingServerCallHandler<RequestT, ResponseT>
    implements ServerCallHandler<InputStream, InputStream> {

    private final GrpcProxy proxy;
    private final ServerCallHandler<InputStream, InputStream> localHandler;
    private final ServerCallHandler<InputStream, InputStream> remoteHandler;

    public ForwardingServerCallHandler(GrpcProxy proxy,
                                       ServerCallHandler<InputStream, InputStream> localHandler,
                                       ServerCallHandler<InputStream, InputStream> remoteHandler) {
        this.proxy = proxy;
        this.localHandler = localHandler;
        this.remoteHandler = remoteHandler;
    }

    @Override
    public Listener<InputStream> startCall(ServerCall<InputStream, InputStream> call, Metadata headers) {
        return proxy.isLeader() ? localHandler.startCall(call, headers) : remoteHandler.startCall(call, headers);
    }
}

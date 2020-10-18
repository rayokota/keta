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

import java.io.InputStream;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.Optional;

import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.MethodDescriptor;
import io.grpc.MethodDescriptor.Marshaller;
import io.grpc.ServerCallHandler;
import io.grpc.ServerMethodDefinition;
import io.grpc.ServerServiceDefinition;
import io.grpc.Channel;
import io.grpc.ServiceDescriptor;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.ServerCalls;
import io.grpc.stub.StreamObserver;
import io.kcache.keta.server.leader.KetaIdentity;

/**
 * Factory for grpc proxy, proxifying all calls to a delegate server.
 */
public class GrpcProxy {

    private KetaIdentity target;
    private ManagedChannel delegateServer;
    private final CallOptions callOptions;

    public GrpcProxy(CallOptions callOptions) {
        this.target = null;
        this.delegateServer = null;
        this.callOptions = callOptions;
    }

    public synchronized boolean isLeader() {
        return target == null;
    }

    public synchronized void setTarget(KetaIdentity target) {
        if (!Objects.equals(this.target, target)) {
            if (delegateServer != null) {
                delegateServer.shutdown();
                delegateServer = null;
            }
            if (target != null) {
                delegateServer = ManagedChannelBuilder.forAddress(target.getHost(), target.getPort())
                    .usePlaintext()
                    .build();
            }
            this.target = target;
        }
    }

    public synchronized Channel getDelegateServer() {
        return delegateServer;
    }

    public CallOptions getCallOptions() {
        return callOptions;
    }

    /**
     * Creates a server service definition for a service proxifying all calls to a delegate server
     *
     * @param serviceDefinition the service definition
     * @return a server definition for the proxy
     */
    public ServerServiceDefinition buildServiceProxy(ServerServiceDefinition serviceDefinition) {
        ServiceDescriptor serviceDescriptor = serviceDefinition.getServiceDescriptor();
        ServerServiceDefinition.Builder builder = ServerServiceDefinition.builder(serviceDescriptor.getName());
        serviceDescriptor.getMethods().forEach(m -> {
            MethodDescriptor<InputStream, InputStream> proxyMethod =
                MethodDescriptor.<InputStream, InputStream>newBuilder()
                    .setType(m.getType())
                    .setFullMethodName(m.getFullMethodName())
                    .setRequestMarshaller(IDENTITY_MARSHALLER)
                    .setResponseMarshaller(IDENTITY_MARSHALLER)
                    .build();
            builder.addMethod(createProxyMethodDefinition(serviceDefinition, proxyMethod));
        });
        return builder.build();
    }

    private ServerMethodDefinition<InputStream, InputStream> createProxyMethodDefinition(
        ServerServiceDefinition serviceDefinition,
        MethodDescriptor<InputStream, InputStream> method) {
        ServerMethodDefinition<?, ?> methodDefinition = serviceDefinition.getMethod(method.getFullMethodName());
        Supplier<ClientCall<InputStream, InputStream>> newClientCall =
            () -> getDelegateServer().newCall(method, getCallOptions());

        ServerCallHandler<InputStream, InputStream> callHandler = null;
        switch (method.getType()) {
            case UNARY:
                callHandler = ServerCalls.asyncUnaryCall(
                    new ServerCalls.UnaryMethod<InputStream, InputStream>() {
                        @Override
                        public void invoke(InputStream request, StreamObserver<InputStream> responseObserver) {
                            ClientCalls.asyncUnaryCall(newClientCall.get(), request, responseObserver);
                        }
                    });
                break;
            case CLIENT_STREAMING:
                callHandler = ServerCalls.asyncClientStreamingCall(
                    new ServerCalls.ClientStreamingMethod<InputStream, InputStream>() {
                        @Override
                        public StreamObserver<InputStream> invoke(StreamObserver<InputStream> responseObserver) {
                            return ClientCalls.asyncClientStreamingCall(newClientCall.get(), responseObserver);
                        }
                    });
                break;
            case SERVER_STREAMING:
                callHandler = ServerCalls.asyncServerStreamingCall(
                    new ServerCalls.ServerStreamingMethod<InputStream, InputStream>() {
                        @Override
                        public void invoke(InputStream request, StreamObserver<InputStream> responseObserver) {
                            ClientCalls.asyncServerStreamingCall(newClientCall.get(), request, responseObserver);
                        }
                    });
                break;
            case BIDI_STREAMING:
                callHandler = ServerCalls.asyncBidiStreamingCall(
                    new ServerCalls.BidiStreamingMethod<InputStream, InputStream>() {
                        @Override
                        public StreamObserver<InputStream> invoke(StreamObserver<InputStream> responseObserver) {
                            return ClientCalls.asyncBidiStreamingCall(newClientCall.get(), responseObserver);
                        }
                    });
                break;
            case UNKNOWN:
                throw new IllegalArgumentException(method.getFullMethodName() + " has unknown type");
        }
        ProxyServerCallHandler<?, ?> proxyHandler = new ProxyServerCallHandler<>(
            methodDefinition.getMethodDescriptor(), headers -> serviceDefinition);
        callHandler = new ForwardingServerCallHandler<>(this, proxyHandler, callHandler);
        // create a reverse proxy method which forwards the byte stream to the delegate server
        return ServerMethodDefinition.create(method, callHandler);
    }

    public static final Marshaller<InputStream> IDENTITY_MARSHALLER =
        new Marshaller<InputStream>() {
            @Override
            public InputStream stream(InputStream value) {
                return value;
            }

            @Override
            public InputStream parse(InputStream stream) {
                return stream;
            }
        };
}

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

import com.google.common.io.ByteStreams;
import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.HandlerRegistry;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerMethodDefinition;
import io.grpc.ServerServiceDefinition;
import io.grpc.Status;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import io.kcache.keta.KetaConfig;
import io.kcache.keta.server.grpc.utils.SslFactory;
import io.kcache.keta.server.leader.KetaIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A grpc-level proxy.
 */
public class GrpcProxy<ReqT, RespT> implements ServerCallHandler<ReqT, RespT> {
    private static final Logger LOG = LoggerFactory.getLogger(GrpcProxy.class);

    private KetaConfig config;
    private KetaIdentity target;
    private ManagedChannel channel;

    public GrpcProxy(KetaConfig config, KetaIdentity target) {
        this.config = config;
        setTarget(target);
    }

    public synchronized KetaIdentity getTarget() {
        return target;
    }

    public synchronized void setTarget(KetaIdentity target) {
        try {
            if (!Objects.equals(this.target, target)) {
                if (channel != null) {
                    LOG.info("Shutting down channel");
                    channel.shutdown();
                    channel = null;
                }
                if (target != null) {
                    LOG.info("Setting up proxy to {}", target);
                    NettyChannelBuilder builder = NettyChannelBuilder.forAddress(target.getHost(), target.getPort());
                    if (target.getScheme().equals("https")) {
                        builder.negotiationType(NegotiationType.TLS)
                            // Need at least trustManager for client
                            .sslContext(new SslFactory(config, false).sslContext());
                    } else {
                        builder.negotiationType(NegotiationType.PLAINTEXT);
                    }
                    channel = builder.build();
                }
                this.target = target;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized ManagedChannel getChannel() {
        return channel;
    }

    @Override
    public ServerCall.Listener<ReqT> startCall(ServerCall<ReqT, RespT> serverCall, Metadata headers) {
        ClientCall<ReqT, RespT> clientCall
            = getChannel().newCall(serverCall.getMethodDescriptor(), CallOptions.DEFAULT);
        CallProxy<ReqT, RespT> proxy = new CallProxy<>(serverCall, clientCall);
        clientCall.start(proxy.clientCallListener, headers);
        serverCall.request(1);
        clientCall.request(1);
        return proxy.serverCallListener;
    }

    private static class CallProxy<ReqT, RespT> {
        final RequestProxy serverCallListener;
        final ResponseProxy clientCallListener;

        public CallProxy(ServerCall<ReqT, RespT> serverCall, ClientCall<ReqT, RespT> clientCall) {
            serverCallListener = new RequestProxy(clientCall);
            clientCallListener = new ResponseProxy(serverCall);
        }

        private class RequestProxy extends ServerCall.Listener<ReqT> {
            private final ClientCall<ReqT, ?> clientCall;
            // Hold 'this' lock when accessing
            private boolean needToRequest;

            public RequestProxy(ClientCall<ReqT, ?> clientCall) {
                this.clientCall = clientCall;
            }

            @Override
            public void onCancel() {
                clientCall.cancel("Server cancelled", null);
            }

            @Override
            public void onHalfClose() {
                clientCall.halfClose();
            }

            @Override
            public void onMessage(ReqT message) {
                clientCall.sendMessage(message);
                synchronized (this) {
                    if (clientCall.isReady()) {
                        clientCallListener.serverCall.request(1);
                    } else {
                        needToRequest = true;
                    }
                }
            }

            @Override
            public void onReady() {
                clientCallListener.onServerReady();
            }

            synchronized void onClientReady() {
                if (needToRequest) {
                    clientCallListener.serverCall.request(1);
                    needToRequest = false;
                }
            }
        }

        private class ResponseProxy extends ClientCall.Listener<RespT> {
            private final ServerCall<?, RespT> serverCall;
            // Hold 'this' lock when accessing
            private boolean needToRequest;

            public ResponseProxy(ServerCall<?, RespT> serverCall) {
                this.serverCall = serverCall;
            }

            @Override
            public void onClose(Status status, Metadata trailers) {
                serverCall.close(status, trailers);
            }

            @Override
            public void onHeaders(Metadata headers) {
                serverCall.sendHeaders(headers);
            }

            @Override
            public void onMessage(RespT message) {
                serverCall.sendMessage(message);
                synchronized (this) {
                    if (serverCall.isReady()) {
                        serverCallListener.clientCall.request(1);
                    } else {
                        needToRequest = true;
                    }
                }
            }

            @Override
            public void onReady() {
                serverCallListener.onClientReady();
            }

            synchronized void onServerReady() {
                if (needToRequest) {
                    serverCallListener.clientCall.request(1);
                    needToRequest = false;
                }
            }
        }
    }

    private static class ByteMarshaller implements MethodDescriptor.Marshaller<byte[]> {
        @Override
        public byte[] parse(InputStream stream) {
            try {
                return ByteStreams.toByteArray(stream);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public InputStream stream(byte[] value) {
            return new ByteArrayInputStream(value);
        }
    }

    public static class Registry extends HandlerRegistry {
        private final MethodDescriptor.Marshaller<byte[]> byteMarshaller = new ByteMarshaller();
        private final GrpcProxy<byte[], byte[]> proxy;
        private final Map<String, ServerMethodDefinition<?, ?>> methods = new HashMap<>();

        public Registry(GrpcProxy<byte[], byte[]> proxy, List<ServerServiceDefinition> services) {
            this.proxy = proxy;
            for (ServerServiceDefinition service : services) {
                for (ServerMethodDefinition<?, ?> method : service.getMethods()) {
                    methods.put(method.getMethodDescriptor().getFullMethodName(), method);
                }
            }
        }

        @Override
        public ServerMethodDefinition<?, ?> lookupMethod(String methodName, String authority) {
            if (proxy == null || proxy.getTarget() == null) {
                LOG.info("Serving {}", methodName);
                return ProxyServerCallHandler.proxyMethod(methods.get(methodName));
            } else {
                LOG.info("Proxying {} to {}", methodName, proxy.getTarget());
                MethodDescriptor<byte[], byte[]> methodDescriptor
                    = MethodDescriptor.newBuilder(byteMarshaller, byteMarshaller)
                    .setFullMethodName(methodName)
                    .setType(MethodDescriptor.MethodType.UNKNOWN)
                    .build();
                return ServerMethodDefinition.create(methodDescriptor, proxy);
            }
        }
    }
}

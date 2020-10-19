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
import io.grpc.MethodDescriptor;
import io.grpc.MethodDescriptor.Marshaller;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerMethodDefinition;
import io.grpc.Status;

import java.io.InputStream;

/**
 * A {@link ServerCallHandler} that handles calls for a particular method.
 *
 * @param <RequestT>  the type of the request payloads
 * @param <ResponseT> the type of the response payloads
 */
public final class ProxyServerCallHandler<RequestT, ResponseT>
    implements ServerCallHandler<InputStream, InputStream> {

    private final ServerMethodDefinition<RequestT, ResponseT> delegateMethod;

    /**
     * Returns a proxy method definition for {@code delegateMethod}.
     *
     * @param delegateMethod the delegate method definition
     */
    public static <RequestT, ResponseT> ServerMethodDefinition<InputStream, InputStream> proxyMethod(
        ServerMethodDefinition<RequestT, ResponseT> delegateMethod) {
        MethodDescriptor<RequestT, ResponseT> delegateMethodDescriptor = delegateMethod.getMethodDescriptor();
        return ServerMethodDefinition.create(
            MethodDescriptor.<InputStream, InputStream>newBuilder()
                .setType(delegateMethodDescriptor.getType())
                .setFullMethodName(delegateMethodDescriptor.getFullMethodName())
                .setRequestMarshaller(IDENTITY_MARSHALLER)
                .setResponseMarshaller(IDENTITY_MARSHALLER)
                .build(),
            new ProxyServerCallHandler<>(delegateMethod));
    }

    ProxyServerCallHandler(ServerMethodDefinition<RequestT, ResponseT> delegateMethod) {
        this.delegateMethod = delegateMethod;
    }

    @Override
    public Listener<InputStream> startCall(ServerCall<InputStream, InputStream> call, Metadata headers) {
        Listener<RequestT> delegateListener =
            delegateMethod
                .getServerCallHandler()
                .startCall(new ServerCallAdapter(call, delegateMethod.getMethodDescriptor()), headers);
        return new ServerCallListenerAdapter(delegateListener);
    }

    private static final Marshaller<InputStream> IDENTITY_MARSHALLER =
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

    /**
     * A {@link Listener} that adapts {@code Listener<RequestT>} to {@code Listener<InputStream>}.
     */
    private final class ServerCallListenerAdapter extends Listener<InputStream> {

        private final Listener<RequestT> delegate;

        public ServerCallListenerAdapter(Listener<RequestT> delegate) {
            this.delegate = delegate;
        }

        @Override
        public void onMessage(InputStream message) {
            delegate.onMessage(delegateMethod.getMethodDescriptor().parseRequest(message));
        }

        @Override
        public void onHalfClose() {
            delegate.onHalfClose();
        }

        @Override
        public void onCancel() {
            delegate.onCancel();
        }

        @Override
        public void onComplete() {
            delegate.onComplete();
        }
    }

    /**
     * A {@link ServerCall} that adapts {@code ServerCall<InputStream>} to {@code
     * ServerCall<ResponseT>}.
     */
    final class ServerCallAdapter extends ServerCall<RequestT, ResponseT> {

        private final ServerCall<InputStream, InputStream> delegate;
        private final MethodDescriptor<RequestT, ResponseT> method;

        ServerCallAdapter(ServerCall<InputStream, InputStream> delegate,
                          MethodDescriptor<RequestT, ResponseT> method) {
            this.delegate = delegate;
            this.method = method;
        }

        @Override
        public MethodDescriptor<RequestT, ResponseT> getMethodDescriptor() {
            return method;
        }

        @Override
        public void request(int numMessages) {
            delegate.request(numMessages);
        }

        @Override
        public void sendHeaders(Metadata headers) {
            delegate.sendHeaders(headers);
        }

        @Override
        public void sendMessage(ResponseT message) {
            delegate.sendMessage(delegateMethod.getMethodDescriptor().streamResponse(message));
        }

        @Override
        public void close(Status status, Metadata trailers) {
            delegate.close(status, trailers);
        }

        @Override
        public boolean isCancelled() {
            return delegate.isCancelled();
        }
    }
}

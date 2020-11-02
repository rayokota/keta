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

import io.etcd.jetcd.api.Event;
import io.etcd.jetcd.api.WatchCancelRequest;
import io.etcd.jetcd.api.WatchCreateRequest;
import io.etcd.jetcd.api.WatchGrpc;
import io.etcd.jetcd.api.WatchRequest;
import io.etcd.jetcd.api.WatchResponse;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import io.kcache.keta.KetaEngine;
import io.kcache.keta.auth.KetaAuthManager;
import io.kcache.keta.server.grpc.errors.KetaErrorType;
import io.kcache.keta.server.grpc.utils.AuthServerInterceptor;
import io.kcache.keta.server.grpc.utils.GrpcUtils;
import io.kcache.keta.server.leader.KetaLeaderElector;
import io.kcache.keta.watch.KetaWatchManager;
import io.kcache.keta.watch.Watch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class WatchService extends WatchGrpc.WatchImplBase {
    private final static Logger LOG = LoggerFactory.getLogger(LeaseService.class);

    private final KetaLeaderElector elector;

    public WatchService(KetaLeaderElector elector) {
        this.elector = elector;
    }

    @Override
    public StreamObserver<WatchRequest> watch(StreamObserver<WatchResponse> responseObserver) {
        if (!KetaEngine.getInstance().isInitialized()) {
            responseObserver.onError((KetaErrorType.Starting.toException()));
            // TODO check this
            return null;
        }
        return new StreamObserver<WatchRequest>() {

            @Override
            public void onNext(WatchRequest request) {
                LOG.debug("received a watchRequest {}", request);
                switch (request.getRequestUnionCase()) {
                    case CREATE_REQUEST:
                        handleCreateRequest(request.getCreateRequest(), responseObserver);
                        break;
                    case CANCEL_REQUEST:
                        handleCancelRequest(request.getCancelRequest(), responseObserver);
                        break;
                    case REQUESTUNION_NOT_SET:
                        LOG.warn("received an empty watch request");
                        break;
                }
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        };
    }

    private void handleCreateRequest(WatchCreateRequest createRequest, StreamObserver<WatchResponse> responseObserver) {
        KetaWatchManager watchMgr = KetaEngine.getInstance().getWatchManager();
        if (watchMgr == null) {
            return;
        }
        KetaAuthManager authMgr = KetaEngine.getInstance().getAuthManager();
        if (authMgr != null) {
            authMgr.checkRangePermitted(AuthServerInterceptor.USER_CTX_KEY.get(),
                createRequest.getKey(), createRequest.getRangeEnd());
        }
        Watch watch = new Watch(0, createRequest.getKey(), createRequest.getRangeEnd());
        watch = watchMgr.add(watch);
        long watchId = watch.getID();
        List<WatchCreateRequest.FilterType> filters = createRequest.getFiltersList();
        boolean prevKv = createRequest.getPrevKv();
        watchMgr.watch(watch, event -> {
            LOG.debug("inside WatchService");
            try {
                List<Event> events = Collections.singletonList(event);
                events = events.stream()
                    .filter(e -> (e.getType() == Event.EventType.PUT
                        && !filters.contains(WatchCreateRequest.FilterType.NOPUT))
                        || (e.getType() == Event.EventType.DELETE
                        && !filters.contains(WatchCreateRequest.FilterType.NODELETE)))
                    .map(e -> prevKv ? e : Event.newBuilder()
                        .mergeFrom(event)
                        .clearPrevKv()
                        .build())
                    .collect(Collectors.toList());
                responseObserver
                    .onNext(WatchResponse.newBuilder()
                        .setHeader(GrpcUtils.toResponseHeader(elector.getMemberId()))
                        .setWatchId(watchId)
                        .addAllEvents(events)
                        .build());
            } catch (StatusRuntimeException e) {
                if (e.getStatus().equals(Status.CANCELLED)) {
                    LOG.warn("connection was closed");
                    return;
                }

                // TODO: is this right?
                LOG.error("caught an error writing response: {}", e.getMessage());
            }
        });
        responseObserver.onNext(WatchResponse.newBuilder()
            .setHeader(GrpcUtils.toResponseHeader(elector.getMemberId()))
            .setWatchId(watchId)
            .setCreated(true)
            .build());
        LOG.debug("successfully registered new Watch");
    }

    private void handleCancelRequest(WatchCancelRequest cancelRequest, StreamObserver<WatchResponse> responseObserver) {
        LOG.debug("cancel watch");
        KetaWatchManager watchMgr = KetaEngine.getInstance().getWatchManager();
        if (watchMgr == null) {
            return;
        }
        long watchId = cancelRequest.getWatchId();
        watchMgr.delete(watchId);
        responseObserver.onNext(WatchResponse.newBuilder()
            .setHeader(GrpcUtils.toResponseHeader(elector.getMemberId()))
            .setWatchId(watchId)
            .setCanceled(true)
            // TODO cancel reason
            .build());
    }
}

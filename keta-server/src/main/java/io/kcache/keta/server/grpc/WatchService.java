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
import io.etcd.jetcd.api.ResponseHeader;
import io.etcd.jetcd.api.WatchCancelRequest;
import io.etcd.jetcd.api.WatchCreateRequest;
import io.etcd.jetcd.api.WatchGrpc;
import io.etcd.jetcd.api.WatchRequest;
import io.etcd.jetcd.api.WatchResponse;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import io.kcache.keta.KetaEngine;
import io.kcache.keta.watch.KetaWatchManager;
import io.kcache.keta.watch.Watch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class WatchService extends WatchGrpc.WatchImplBase {
    private final static Logger LOG = LoggerFactory.getLogger(LeaseService.class);

    @Override
    public StreamObserver<WatchRequest> watch(StreamObserver<WatchResponse> responseObserver) {
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
        Watch watch = new Watch(0, createRequest.getKey().toByteArray(), createRequest.getRangeEnd().toByteArray());
        watch = watchMgr.add(watch);
        long watchId = watch.getId();
        List<WatchCreateRequest.FilterType> filters = createRequest.getFiltersList();
        boolean prevKv = createRequest.getPrevKv();
        watchMgr.watch(watch, event -> {
            LOG.info("inside WatchService");
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
                        // TODO add headers everywhere
                        .setHeader(ResponseHeader.newBuilder().build())
                        .setWatchId(watchId)
                        .addAllEvents(events)
                        .build());
            } catch (StatusRuntimeException e) {
                if (e.getStatus().equals(Status.CANCELLED)) {
                    LOG.warn("connection was closed");
                    return;
                }

                LOG.error("caught an error writing response: {}", e.getMessage());
            }
        });
        responseObserver.onNext(WatchResponse.newBuilder()
            .setHeader(ResponseHeader.newBuilder().build())
            .setWatchId(watchId)
            .setCreated(true)
            .build());
        LOG.info("successfully registered new Watch");
    }

    private void handleCancelRequest(WatchCancelRequest cancelRequest, StreamObserver<WatchResponse> responseObserver) {
        LOG.info("cancel watch");
        KetaWatchManager watchMgr = KetaEngine.getInstance().getWatchManager();
        long watchId = cancelRequest.getWatchId();
        watchMgr.delete(watchId);
        responseObserver.onNext(WatchResponse.newBuilder()
            .setHeader(ResponseHeader.newBuilder().build())
            .setWatchId(watchId)
            .setCanceled(true)
            // TODO cancel reason
            .build());
    }
}

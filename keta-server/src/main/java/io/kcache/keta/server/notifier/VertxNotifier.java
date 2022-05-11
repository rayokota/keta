/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.kcache.keta.server.notifier;

import com.google.protobuf.InvalidProtocolBufferException;
import io.etcd.jetcd.api.Event;
import io.kcache.keta.notifier.AbstractNotifier;
import io.kcache.keta.notifier.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A Notifier that use Vertx Event Bus
 */
public class VertxNotifier extends AbstractNotifier {
    private static final Logger LOG = LoggerFactory.getLogger(VertxNotifier.class);

    private final EventBus eventBus;

    private final Map<Long, MessageConsumer<byte[]>> consumers;

    public VertxNotifier(EventBus eventBus) {
        this.eventBus = eventBus;
        this.consumers = new ConcurrentHashMap<>();
    }

    @Override
    public void publish(long watchID, Event event) {
        LOG.info("publishing to {}", watchID);
        this.eventBus.publish(String.valueOf(watchID), event.toByteArray());
    }

    @Override
    public void watch(long watchID, Handler<Event> handler) {
        LOG.info("listening on {}", watchID);
        MessageConsumer<byte[]> consumer = this.eventBus.consumer(String.valueOf(watchID), message -> {
            LOG.info("received a message from the eventbus: '{}'", message);
            if (message.body() instanceof byte[]) {
                try {
                    Event event = Event.newBuilder()
                        .mergeFrom(message.body())
                        .build();
                    handler.handle(event);
                } catch (InvalidProtocolBufferException e) {
                    LOG.error("cannot create Event: '{}', skipping", e.toString());
                }
            } else {
                LOG.error("received a message which is not byte[], skipping");
            }
        });
        consumers.put(watchID, consumer);
    }

    @Override
    public void unwatch(long watchID) {
        MessageConsumer<byte[]> consumer = consumers.remove(watchID);
        consumer.unregister();
    }
}

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

package io.kcache.keta.notifier;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import io.etcd.jetcd.api.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A Notifier that use Guava Event Bus
 */
public class GuavaNotifier extends AbstractNotifier {
    private static final Logger LOG = LoggerFactory.getLogger(GuavaNotifier.class);

    private final EventBus eventBus;

    private final Map<Long, WatchEventConsumer> consumers;

    public GuavaNotifier(EventBus eventBus) {
        this.eventBus = eventBus;
        this.consumers = new ConcurrentHashMap<>();
    }

    @Override
    public void publish(long watchID, Event event) {
        LOG.info("publishing to {}", watchID);
        eventBus.post(new WatchEvent(watchID, event));
    }

    @Override
    public void watch(long watchID, Handler<Event> handler) {
        LOG.info("listening on {}", watchID);
        WatchEventConsumer consumer = new WatchEventConsumer(watchID, handler);
        eventBus.register(consumer);
        consumers.put(watchID, consumer);
    }

    @Override
    public void unwatch(long watchID) {
        WatchEventConsumer consumer = consumers.remove(watchID);
        eventBus.unregister(consumer);
    }

    static class WatchEventConsumer {
        private final long watchID;
        private final Handler<Event> handler;

        public WatchEventConsumer(long watchID, Handler<Event> handler) {
            this.watchID = watchID;
            this.handler = handler;
        }

        @Subscribe
        public void consume(WatchEvent e) {
            if (watchID == e.watchID) {
                handler.handle(e.getEvent());
            }
        }
    }

    static class WatchEvent {
        private final long watchID;
        private final Event event;

        public WatchEvent(long watchID, Event event) {
            this.watchID = watchID;
            this.event = event;
        }

        public long getWatchID() {
            return watchID;
        }

        public Event getEvent() {
            return event;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            WatchEvent that = (WatchEvent) o;
            return watchID == that.watchID && Objects.equals(event, that.event);
        }

        @Override
        public int hashCode() {
            return Objects.hash(watchID, event);
        }
    }
}

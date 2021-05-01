/*
 * Copyright 2016-2020 The jetcd authors
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

package io.kcache.keta.server;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.Watch.Watcher;
import io.etcd.jetcd.common.exception.EtcdException;
import io.etcd.jetcd.watch.WatchResponse;
import io.kcache.keta.server.utils.RemoteClusterTestHarness;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.Ignore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import static io.kcache.keta.server.utils.TestUtils.randomByteSequence;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(VertxExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class WatchErrorTest extends RemoteClusterTestHarness {

    private Client client;

    @BeforeAll
    public void deployVerticle(Vertx vertx, VertxTestContext testContext) throws Exception {
        vertx.deployVerticle(createKeta(), testContext.completing());
        client = Client.builder().endpoints(endpoints).build();
    }

    @BeforeEach
    public void setUp(Vertx vertx) throws Exception {
        super.setUp(vertx);
    }

    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Ignore
    @Test
    public void testWatchOnError() throws Exception {
        final ByteSequence key = randomByteSequence();
        final List<Throwable> events = new ArrayList<>();
        final CountDownLatch latch = new CountDownLatch(1);

        Consumer<WatchResponse> onNext = r -> {
            // no-op
        };
        Consumer<Throwable> onError = t -> {
            events.add(t);
            latch.countDown();
        };

        try (Watcher watcher = client.getWatchClient().watch(key, onNext, onError)) {
            tearDown();
            latch.await();
        }

        assertThat(events).isNotEmpty();
        assertThat(events).allMatch(EtcdException.class::isInstance);
    }
}

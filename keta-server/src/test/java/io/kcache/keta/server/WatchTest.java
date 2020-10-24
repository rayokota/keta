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

import com.google.protobuf.ByteString;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.Watch.Watcher;
import io.etcd.jetcd.common.exception.CompactedException;
import io.etcd.jetcd.kv.PutResponse;
import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.watch.WatchEvent;
import io.etcd.jetcd.watch.WatchEvent.EventType;
import io.etcd.jetcd.watch.WatchResponse;
import io.kcache.keta.server.utils.RemoteClusterTestHarness;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static io.kcache.keta.server.utils.TestUtils.bytesOf;
import static io.kcache.keta.server.utils.TestUtils.randomByteSequence;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(VertxExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class WatchTest extends RemoteClusterTestHarness {

    public static final ByteSequence namespace = bytesOf("test-namespace/");

    private Client client;

    @BeforeAll
    public void deployVerticle(Vertx vertx, VertxTestContext testContext) throws Exception {
        vertx.deployVerticle(createKeta(), testContext.completing());
        client = Client.builder().endpoints(ENDPOINTS).build();
    }

    @BeforeEach
    public void setUp(Vertx vertx) throws Exception {
        super.setUp(vertx);
    }

    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testNamespacedAndNotNamespacedClient() throws Exception {
        final ByteSequence key = randomByteSequence();
        final ByteSequence nsKey = ByteSequence.from(ByteString.copyFrom(namespace.getBytes()).concat(ByteString.copyFrom(key.getBytes())));
        final Client client = Client.builder().endpoints(ENDPOINTS).build();
        final Client nsClient = Client.builder().endpoints(ENDPOINTS).namespace(namespace).build();

        final CountDownLatch latch = new CountDownLatch(1);
        final ByteSequence value = randomByteSequence();
        final AtomicReference<WatchResponse> ref = new AtomicReference<>();

        // From client with namespace watch for key. Since client is namespaced it should watch for namespaced key.
        try (Watcher watcher = nsClient.getWatchClient().watch(key, response -> {
            ref.set(response);
            latch.countDown();
        })) {

            // Using non-namespaced client put namespaced key.
            client.getKVClient().put(nsKey, value).get();
            latch.await(4, TimeUnit.SECONDS);

            assertThat(ref.get()).isNotNull();
            assertThat(ref.get().getEvents().size()).isEqualTo(1);
            assertThat(ref.get().getEvents().get(0).getEventType()).isEqualTo(EventType.PUT);
            assertThat(ref.get().getEvents().get(0).getKeyValue().getKey()).isEqualTo(key);
        }
    }

    @Test
    public void testWatchOnPut() throws Exception {
        final ByteSequence key = randomByteSequence();
        final CountDownLatch latch = new CountDownLatch(1);
        final ByteSequence value = randomByteSequence();
        final AtomicReference<WatchResponse> ref = new AtomicReference<>();

        try (Watcher watcher = client.getWatchClient().watch(key, response -> {
            ref.set(response);
            latch.countDown();
        })) {

            client.getKVClient().put(key, value).get();
            latch.await(4, TimeUnit.SECONDS);

            assertThat(ref.get()).isNotNull();
            assertThat(ref.get().getEvents().size()).isEqualTo(1);
            assertThat(ref.get().getEvents().get(0).getEventType()).isEqualTo(EventType.PUT);
            assertThat(ref.get().getEvents().get(0).getKeyValue().getKey()).isEqualTo(key);
        }
    }

    @Test
    public void testMultipleWatch() throws Exception {
        final ByteSequence key = randomByteSequence();
        final CountDownLatch latch = new CountDownLatch(2);
        final ByteSequence value = randomByteSequence();
        final List<WatchResponse> res = Collections.synchronizedList(new ArrayList<>(2));

        try (Watcher w1 = client.getWatchClient().watch(key, response -> {
            res.add(response);
            latch.countDown();
        })) {

            try (Watcher w2 = client.getWatchClient().watch(key, response -> {
                res.add(response);
                latch.countDown();
            })) {

                client.getKVClient().put(key, value).get();
                latch.await(4, TimeUnit.SECONDS);

                assertThat(res).hasSize(2);
                //assertThat(res.get(0)).usingRecursiveComparison().isEqualTo(res.get(1));
                assertThat(res.get(0).getEvents().size()).isEqualTo(1);
                assertThat(res.get(0).getEvents().get(0).getEventType()).isEqualTo(EventType.PUT);
                assertThat(res.get(0).getEvents().get(0).getKeyValue().getKey()).isEqualTo(key);
            }
        }
    }

    @Test
    public void testWatchOnDelete() throws Exception {
        final ByteSequence key = randomByteSequence();
        final CountDownLatch latch = new CountDownLatch(1);
        final ByteSequence value = randomByteSequence();
        final AtomicReference<WatchResponse> ref = new AtomicReference<>();

        client.getKVClient().put(key, value).get();

        try (Watcher watcher = client.getWatchClient().watch(key, response -> {
            ref.set(response);
            latch.countDown();
        })) {
            client.getKVClient().delete(key).get();

            latch.await(4, TimeUnit.SECONDS);

            assertThat(ref.get()).isNotNull();
            assertThat(ref.get().getEvents().size()).isEqualTo(1);

            WatchEvent event = ref.get().getEvents().get(0);
            assertThat(event.getEventType()).isEqualTo(EventType.DELETE);
            assertThat(Arrays.equals(event.getKeyValue().getKey().getBytes(), key.getBytes())).isTrue();
        }
    }

    @Test
    @Disabled
    public void testWatchCompacted() throws Exception {
        final ByteSequence key = randomByteSequence();
        final ByteSequence value = randomByteSequence();

        // Insert key twice to ensure we have at least two revisions
        client.getKVClient().put(key, value).get();
        final PutResponse putResponse = client.getKVClient().put(key, value).get();
        // Compact until latest revision
        client.getKVClient().compact(putResponse.getHeader().getRevision()).get();

        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Throwable> ref = new AtomicReference<>();
        // Try to listen from previous revision on
        final WatchOption watchOption = WatchOption.newBuilder().withRevision(putResponse.getHeader().getRevision() - 1)
            .build();
        try (Watcher watcher = client.getWatchClient().watch(key, watchOption, Watch.listener(response -> {
        }, error -> {
            ref.set(error);
            latch.countDown();
        }))) {
            latch.await(4, TimeUnit.SECONDS);
            // Expect CompactedException
            assertThat(ref.get()).isNotNull();
            assertThat(ref.get().getClass()).isEqualTo(CompactedException.class);
        }
    }

    @Test
    public void testWatchClose() throws Exception {
        final ByteSequence key = randomByteSequence();
        final ByteSequence value = randomByteSequence();
        final List<WatchResponse> events = new ArrayList<>();

        final CountDownLatch l1 = new CountDownLatch(1);
        final CountDownLatch l2 = new CountDownLatch(1);

        try (Watcher watcher = client.getWatchClient().watch(key, response -> {
            events.add(response);
            l1.countDown();
        })) {
            client.getKVClient().put(key, value).get();
            l1.await();
        }

        client.getKVClient().put(key, randomByteSequence()).get();
        l2.await(4, TimeUnit.SECONDS);

        assertThat(events).hasSize(1);
        assertThat(events.get(0).getEvents()).hasSize(1);
        assertThat(events.get(0).getEvents().get(0).getEventType()).isEqualTo(EventType.PUT);
        assertThat(events.get(0).getEvents().get(0).getKeyValue().getKey()).isEqualTo(key);
        assertThat(events.get(0).getEvents().get(0).getKeyValue().getValue()).isEqualTo(value);
    }
}

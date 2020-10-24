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

import com.google.common.base.Charsets;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.Lease;
import io.etcd.jetcd.lease.LeaseKeepAliveResponse;
import io.etcd.jetcd.lease.LeaseTimeToLiveResponse;
import io.etcd.jetcd.options.LeaseOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.support.CloseableClient;
import io.etcd.jetcd.support.Observers;
import io.grpc.stub.StreamObserver;
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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@ExtendWith(VertxExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LeaseTest extends RemoteClusterTestHarness {

    private KV kvClient;
    private Client client;
    private Lease leaseClient;

    private static final ByteSequence KEY = ByteSequence.from("foo", Charsets.UTF_8);
    private static final ByteSequence KEY_2 = ByteSequence.from("foo2", Charsets.UTF_8);
    private static final ByteSequence VALUE = ByteSequence.from("bar", Charsets.UTF_8);

    @BeforeAll
    public void deployVerticle(Vertx vertx, VertxTestContext testContext) throws Exception {
        vertx.deployVerticle(createKeta(), testContext.completing());
        //TODO
        client = Client.builder().endpoints(ENDPOINTS).build();
        kvClient = client.getKVClient();
        leaseClient = client.getLeaseClient();
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
    public void testGrant() throws Exception {
        long leaseID = leaseClient.grant(5).get().getID();

        kvClient.put(KEY, VALUE, PutOption.newBuilder().withLeaseId(leaseID).build()).get();
        assertThat(kvClient.get(KEY).get().getCount()).isEqualTo(1);

        Thread.sleep(6000);
        assertThat(kvClient.get(KEY).get().getCount()).isEqualTo(0);
    }

    @Test
    @Disabled
    public void testGrantWithTimeout() throws Exception {
        long leaseID = leaseClient.grant(5, 10, TimeUnit.SECONDS).get().getID();
        kvClient.put(KEY, VALUE, PutOption.newBuilder().withLeaseId(leaseID).build()).get();
        assertThat(kvClient.get(KEY).get().getCount()).isEqualTo(1);

        Thread.sleep(6000L);
        assertThat(kvClient.get(KEY).get().getCount()).isEqualTo(0);

        tearDown();
        assertThatExceptionOfType(ExecutionException.class)
            .isThrownBy(() -> leaseClient.grant(5, 2, TimeUnit.SECONDS).get().getID());
        setUp();
    }

    @Test
    public void testRevoke() throws Exception {
        long leaseID = leaseClient.grant(5).get().getID();
        kvClient.put(KEY, VALUE, PutOption.newBuilder().withLeaseId(leaseID).build()).get();
        assertThat(kvClient.get(KEY).get().getCount()).isEqualTo(1);
        leaseClient.revoke(leaseID).get();
        assertThat(kvClient.get(KEY).get().getCount()).isEqualTo(0);
    }

    @Test
    public void testKeepAliveOnce() throws ExecutionException, InterruptedException {
        long leaseID = leaseClient.grant(2).get().getID();
        kvClient.put(KEY, VALUE, PutOption.newBuilder().withLeaseId(leaseID).build()).get();
        assertThat(kvClient.get(KEY).get().getCount()).isEqualTo(1);
        LeaseKeepAliveResponse rp = leaseClient.keepAliveOnce(leaseID).get();
        assertThat(rp.getTTL()).isGreaterThan(0);
    }

    @Test
    public void testKeepAlive() throws ExecutionException, InterruptedException {
        long leaseID = leaseClient.grant(2).get().getID();
        kvClient.put(KEY, VALUE, PutOption.newBuilder().withLeaseId(leaseID).build()).get();
        assertThat(kvClient.get(KEY).get().getCount()).isEqualTo(1);

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<LeaseKeepAliveResponse> responseRef = new AtomicReference<>();
        StreamObserver<LeaseKeepAliveResponse> observer = Observers.observer(response -> {
            responseRef.set(response);
            latch.countDown();
        });

        try (CloseableClient c = leaseClient.keepAlive(leaseID, observer)) {
            latch.await(5, TimeUnit.SECONDS);
            LeaseKeepAliveResponse response = responseRef.get();
            assertThat(response.getTTL()).isGreaterThan(0);
        }

        Thread.sleep(3000);
        assertThat(kvClient.get(KEY).get().getCount()).isEqualTo(0);
    }

    @Test
    public void testTimeToLive() throws ExecutionException, InterruptedException {
        long ttl = 5;
        long leaseID = leaseClient.grant(ttl).get().getID();
        LeaseTimeToLiveResponse resp = leaseClient.timeToLive(leaseID, LeaseOption.DEFAULT).get();
        assertThat(resp.getTTl()).isGreaterThan(0);
        assertThat(resp.getGrantedTTL()).isEqualTo(ttl);
    }

    @Test
    public void testTimeToLiveWithKeys() throws ExecutionException, InterruptedException {
        long ttl = 5;
        long leaseID = leaseClient.grant(ttl).get().getID();
        PutOption putOption = PutOption.newBuilder().withLeaseId(leaseID).build();
        kvClient.put(KEY_2, VALUE, putOption).get();

        LeaseOption leaseOption = LeaseOption.newBuilder().withAttachedKeys().build();
        LeaseTimeToLiveResponse resp = leaseClient.timeToLive(leaseID, leaseOption).get();
        assertThat(resp.getTTl()).isGreaterThan(0);
        assertThat(resp.getGrantedTTL()).isEqualTo(ttl);
        assertThat(resp.getKeys().size()).isEqualTo(1);
        assertThat(resp.getKeys().get(0)).isEqualTo(KEY_2);
    }
}

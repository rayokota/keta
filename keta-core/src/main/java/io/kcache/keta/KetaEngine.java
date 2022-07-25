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
package io.kcache.keta;

import io.etcd.jetcd.api.Role;
import io.etcd.jetcd.api.User;
import io.kcache.Cache;
import io.kcache.KafkaCache;
import io.kcache.KafkaCacheConfig;
import io.kcache.keta.auth.KetaAuthManager;
import io.kcache.keta.kafka.serialization.KafkaProtobufSerde;
import io.kcache.keta.kafka.serialization.KafkaValueSerde;
import io.kcache.keta.leader.LeaderElector;
import io.kcache.keta.lease.KetaLeaseManager;
import io.kcache.keta.notifier.Notifier;
import io.kcache.keta.pb.Lease;
import io.kcache.keta.transaction.KetaCommitTable;
import io.kcache.keta.transaction.KetaTimestampStorage;
import io.kcache.keta.transaction.client.KetaTransactionManager;
import io.kcache.keta.version.TxVersionedCache;
import io.kcache.keta.version.VersionedCache;
import io.kcache.keta.version.VersionedValues;
import io.kcache.keta.watch.KetaWatchManager;
import io.kcache.utils.Caches;
import io.kcache.utils.InMemoryCache;
import org.apache.kafka.common.Configurable;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Utils;
import org.apache.omid.committable.CommitTable;
import org.apache.omid.timestamp.storage.TimestampStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class KetaEngine implements Configurable, Closeable {
    private static final Logger LOG = LoggerFactory.getLogger(KetaEngine.class);

    private KetaConfig config;
    private LeaderElector elector;
    private Cache<Long, Long> commits;
    private Cache<Long, Long> timestamps;
    private Cache<Long, Lease> leases;
    private Cache<String, String> auth;
    private Cache<String, User> authUsers;
    private Cache<String, Role> authRoles;
    private Cache<byte[], VersionedValues> cache;
    private TxVersionedCache txCache;
    private KetaTransactionManager transactionManager;
    private KetaAuthManager authManager;
    private KetaLeaseManager leaseManager;
    private KetaWatchManager watchManager;
    private final AtomicBoolean initialized = new AtomicBoolean();

    private static KetaEngine INSTANCE;

    public synchronized static KetaEngine getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new KetaEngine();
        }
        return INSTANCE;
    }

    public synchronized static void closeInstance() {
        if (INSTANCE != null) {
            try {
                INSTANCE.close();
            } catch (IOException e) {
                LOG.warn("Could not close engine", e);
            }
            INSTANCE = null;
        }
    }

    private KetaEngine() {
    }

    public void configure(Map<String, ?> configs) {
        configure(new KetaConfig(configs));
    }

    public void configure(KetaConfig config) {
        this.config = config;
    }

    public void init(LeaderElector elector, Notifier notifier) {
        this.elector = elector;
        this.watchManager = new KetaWatchManager(notifier);
        Map<String, Object> configs = config.originals();
        String bootstrapServers = (String) configs.get(KafkaCacheConfig.KAFKACACHE_BOOTSTRAP_SERVERS_CONFIG);
        String groupId = (String) configs.getOrDefault(KafkaCacheConfig.KAFKACACHE_GROUP_ID_CONFIG, "keta-1");

        CompletableFuture<Void> commitsFuture = CompletableFuture.runAsync(() ->
            commits = initCommits(new HashMap<>(configs), bootstrapServers, groupId));
        CompletableFuture<Void> timestampsFuture = CompletableFuture.runAsync(() ->
            timestamps = initTimestamps(new HashMap<>(configs), bootstrapServers, groupId));
        CompletableFuture<Void> leasesFuture = CompletableFuture.runAsync(() ->
            leases = initLeases(new HashMap<>(configs), bootstrapServers, groupId));
        CompletableFuture<Void> authFuture = CompletableFuture.runAsync(() ->
            auth = initAuth(new HashMap<>(configs), bootstrapServers, groupId));
        CompletableFuture<Void> authUsersFuture = CompletableFuture.runAsync(() ->
            authUsers = initAuthUsers(new HashMap<>(configs), bootstrapServers, groupId));
        CompletableFuture<Void> authRolesFuture = CompletableFuture.runAsync(() ->
            authRoles = initAuthRoles(new HashMap<>(configs), bootstrapServers, groupId));
        CompletableFuture<Void> kvFuture = CompletableFuture.runAsync(() ->
            cache = initKv(notifier, new HashMap<>(configs), bootstrapServers, groupId));
        CompletableFuture.allOf(commitsFuture, timestampsFuture, leasesFuture,
            authFuture, authUsersFuture, authRolesFuture, kvFuture).join();

        txCache = new TxVersionedCache(new VersionedCache("keta", cache));
        CommitTable commitTable = new KetaCommitTable(commits);
        TimestampStorage timestampStorage = new KetaTimestampStorage(timestamps);
        transactionManager = KetaTransactionManager.newInstance(commitTable, timestampStorage);
        leaseManager = new KetaLeaseManager(txCache, leases);
        authManager = new KetaAuthManager(config, auth, authUsers, authRoles);

        boolean isInitialized = initialized.compareAndSet(false, true);
        if (!isInitialized) {
            throw new IllegalStateException("Illegal state while initializing engine. Engine "
                + "was already initialized");
        }
    }

    private Cache<Long, Long> initCommits(Map<String, Object> configs, String bootstrapServers, String groupId) {
        Cache<Long, Long> commits;
        if (bootstrapServers != null) {
            String topic = "_keta_commits";
            configs.put(KafkaCacheConfig.KAFKACACHE_TOPIC_CONFIG, topic);
            configs.put(KafkaCacheConfig.KAFKACACHE_GROUP_ID_CONFIG, groupId);
            configs.put(KafkaCacheConfig.KAFKACACHE_CLIENT_ID_CONFIG, groupId + "-" + topic);
            commits = new KafkaCache<>(
                new KafkaCacheConfig(configs), Serdes.Long(), Serdes.Long(), null, new InMemoryCache<>());
        } else {
            commits = new InMemoryCache<>();
        }
        commits = Caches.concurrentCache(commits);
        commits.init();
        return commits;
    }

    private Cache<Long, Long> initTimestamps(Map<String, Object> configs, String bootstrapServers, String groupId) {
        Cache<Long, Long> timestamps;
        if (bootstrapServers != null) {
            String topic = "_keta_timestamps";
            configs.put(KafkaCacheConfig.KAFKACACHE_TOPIC_CONFIG, topic);
            configs.put(KafkaCacheConfig.KAFKACACHE_GROUP_ID_CONFIG, groupId);
            configs.put(KafkaCacheConfig.KAFKACACHE_CLIENT_ID_CONFIG, groupId + "-" + topic);
            timestamps = new KafkaCache<>(
                new KafkaCacheConfig(configs), Serdes.Long(), Serdes.Long(), null, new InMemoryCache<>());
        } else {
            timestamps = new InMemoryCache<>();
        }
        timestamps = Caches.concurrentCache(timestamps);
        timestamps.init();
        return timestamps;
    }

    private Cache<Long, Lease> initLeases(Map<String, Object> configs, String bootstrapServers, String groupId) {
        Cache<Long, Lease> leases;
        if (bootstrapServers != null) {
            String topic = "_keta_leases";
            configs.put(KafkaCacheConfig.KAFKACACHE_TOPIC_CONFIG, topic);
            configs.put(KafkaCacheConfig.KAFKACACHE_GROUP_ID_CONFIG, groupId);
            configs.put(KafkaCacheConfig.KAFKACACHE_CLIENT_ID_CONFIG, groupId + "-" + topic);
            leases = new KafkaCache<>(
                new KafkaCacheConfig(configs), Serdes.Long(), new KafkaProtobufSerde<>(Lease.class), null, new InMemoryCache<>());
        } else {
            leases = new InMemoryCache<>();
        }
        leases = Caches.concurrentCache(leases);
        leases.init();
        return leases;
    }

    private Cache<String, String> initAuth(Map<String, Object> configs, String bootstrapServers, String groupId) {
        Cache<String, String> auth;
        if (bootstrapServers != null) {
            String topic = "_keta_auth";
            configs.put(KafkaCacheConfig.KAFKACACHE_TOPIC_CONFIG, topic);
            configs.put(KafkaCacheConfig.KAFKACACHE_GROUP_ID_CONFIG, groupId);
            configs.put(KafkaCacheConfig.KAFKACACHE_CLIENT_ID_CONFIG, groupId + "-" + topic);
            auth = new KafkaCache<>(
                new KafkaCacheConfig(configs), Serdes.String(), Serdes.String(), null, new InMemoryCache<>());
        } else {
            auth = new InMemoryCache<>();
        }
        auth = Caches.concurrentCache(auth);
        auth.init();
        return auth;
    }

    private Cache<String, User> initAuthUsers(Map<String, Object> configs, String bootstrapServers, String groupId) {
        Cache<String, User> authUsers;
        if (bootstrapServers != null) {
            String topic = "_keta_auth_users";
            configs.put(KafkaCacheConfig.KAFKACACHE_TOPIC_CONFIG, topic);
            configs.put(KafkaCacheConfig.KAFKACACHE_GROUP_ID_CONFIG, groupId);
            configs.put(KafkaCacheConfig.KAFKACACHE_CLIENT_ID_CONFIG, groupId + "-" + topic);
            authUsers = new KafkaCache<>(
                new KafkaCacheConfig(configs), Serdes.String(), new KafkaProtobufSerde<>(User.class), null, new InMemoryCache<>());
        } else {
            authUsers = new InMemoryCache<>();
        }
        authUsers = Caches.concurrentCache(authUsers);
        authUsers.init();
        return authUsers;
    }

    private Cache<String, Role> initAuthRoles(Map<String, Object> configs, String bootstrapServers, String groupId) {
        Cache<String, Role> authRoles;
        if (bootstrapServers != null) {
            String topic = "_keta_auth_roles";
            configs.put(KafkaCacheConfig.KAFKACACHE_TOPIC_CONFIG, topic);
            configs.put(KafkaCacheConfig.KAFKACACHE_GROUP_ID_CONFIG, groupId);
            configs.put(KafkaCacheConfig.KAFKACACHE_CLIENT_ID_CONFIG, groupId + "-" + topic);
            authRoles = new KafkaCache<>(
                new KafkaCacheConfig(configs), Serdes.String(), new KafkaProtobufSerde<>(Role.class), null, new InMemoryCache<>());
        } else {
            authRoles = new InMemoryCache<>();
        }
        authRoles = Caches.concurrentCache(authRoles);
        authRoles.init();
        return authRoles;
    }

    private Cache<byte[], VersionedValues> initKv(
        Notifier notifier, Map<String, Object> configs, String bootstrapServers, String groupId) {
        Cache<byte[], VersionedValues> cache;
        if (bootstrapServers != null) {
            String topic = "_keta_kv";
            configs.put(KafkaCacheConfig.KAFKACACHE_TOPIC_CONFIG, topic);
            configs.put(KafkaCacheConfig.KAFKACACHE_GROUP_ID_CONFIG, groupId);
            configs.put(KafkaCacheConfig.KAFKACACHE_CLIENT_ID_CONFIG, groupId + "-" + topic);
            Comparator<byte[]> cmp = VersionedCache.BYTES_COMPARATOR;
            cache = new KafkaCache<>(
                new KafkaCacheConfig(configs), Serdes.ByteArray(), new KafkaValueSerde(), notifier, topic, cmp);
        } else {
            cache = new InMemoryCache<>();
        }
        cache.init();
        return cache;
    }

    public boolean isInitialized() {
        return initialized.get();
    }

    public void sync() {
        CompletableFuture<Void> commitsFuture = CompletableFuture
            .runAsync(() -> commits.reset())
            .thenRunAsync(() -> commits.sync());
        CompletableFuture<Void> timestampsFuture = CompletableFuture
            .runAsync(() -> timestamps.reset())
            .thenRunAsync(() -> timestamps.sync())
            .thenRunAsync(() -> transactionManager.init());
        CompletableFuture<Void> leasesFuture = CompletableFuture
            .runAsync(() -> leases.reset())
            .thenRunAsync(() -> leases.sync());
        CompletableFuture<Void> authFuture = CompletableFuture
            .runAsync(() -> auth.reset())
            .thenRunAsync(() -> auth.sync());
        CompletableFuture<Void> authUsersFuture = CompletableFuture
            .runAsync(() -> authUsers.reset())
            .thenRunAsync(() -> authUsers.sync());
        CompletableFuture<Void> authRolesFuture = CompletableFuture
            .runAsync(() -> authRoles.reset())
            .thenRunAsync(() -> authRoles.sync());
        CompletableFuture<Void> kvFuture = CompletableFuture
            .runAsync(() -> cache.reset())
            .thenRunAsync(() -> cache.sync());
        CompletableFuture.allOf(commitsFuture, timestampsFuture, leasesFuture,
            authFuture, authUsersFuture, authRolesFuture, kvFuture).join();
    }

    public boolean isLeader() {
        return isInitialized() && elector.isLeader();
    }

    public TxVersionedCache getTxCache() {
        return txCache;
    }

    public KetaTransactionManager getTxManager() {
        return transactionManager;
    }

    public KetaAuthManager getAuthManager() {
        return authManager;
    }

    public KetaLeaseManager getLeaseManager() {
        return leaseManager;
    }

    public KetaWatchManager getWatchManager() {
        return watchManager;
    }

    @Override
    public void close() throws IOException {
        if (transactionManager != null) {
            transactionManager.close();
        }
        if (cache != null) {
            cache.close();
        }
        if (authRoles != null) {
            authRoles.close();
        }
        if (authUsers != null) {
            authUsers.close();
        }
        if (auth != null) {
            auth.close();
        }
        if (leases != null) {
            leases.close();
        }
        if (timestamps != null) {
            timestamps.close();
        }
        if (commits != null) {
            commits.close();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getConfiguredInstance(String className, Map<String, ?> configs) {
        try {
            Class<T> cls = (Class<T>) Class.forName(className);
            Object o = Utils.newInstance(cls);
            if (o instanceof Configurable) {
                ((Configurable) o).configure(configs);
            }
            return cls.cast(o);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}

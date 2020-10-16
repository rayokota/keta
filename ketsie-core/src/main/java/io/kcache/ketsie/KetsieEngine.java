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
package io.kcache.ketsie;

import io.kcache.Cache;
import io.kcache.KafkaCache;
import io.kcache.KafkaCacheConfig;
import io.kcache.ketsie.kafka.serialization.KafkaLeaseSerde;
import io.kcache.ketsie.kafka.serialization.KafkaValueSerde;
import io.kcache.ketsie.lease.KetsieLeaseManager;
import io.kcache.ketsie.lease.Lease;
import io.kcache.ketsie.transaction.KetsieCommitTable;
import io.kcache.ketsie.transaction.KetsieTimestampStorage;
import io.kcache.ketsie.transaction.client.KetsieTransactionManager;
import io.kcache.ketsie.version.TxVersionedCache;
import io.kcache.ketsie.version.VersionedCache;
import io.kcache.ketsie.version.VersionedValues;
import io.kcache.utils.Caches;
import io.kcache.utils.InMemoryCache;
import io.kcache.utils.TransformedRawCache;
import org.apache.kafka.common.Configurable;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Utils;
import org.apache.omid.committable.CommitTable;
import org.apache.omid.timestamp.storage.TimestampStorage;
import org.apache.omid.transaction.RollbackException;
import org.apache.omid.transaction.Transaction;
import org.apache.omid.transaction.TransactionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class KetsieEngine implements Configurable, Closeable {
    private static final Logger LOG = LoggerFactory.getLogger(KetsieEngine.class);

    private KetsieConfig config;
    private Cache<Long, Long> commits;
    private Cache<Long, Long> timestamps;
    private Cache<Long, Lease> leases;
    private Cache<byte[], VersionedValues> cache;
    private TxVersionedCache txCache;
    private KetsieTransactionManager transactionManager;
    private KetsieLeaseManager leaseManager;
    private final AtomicBoolean initialized = new AtomicBoolean();

    private static KetsieEngine INSTANCE;

    public synchronized static KetsieEngine getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new KetsieEngine();
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

    private KetsieEngine() {
    }

    public void configure(Map<String, ?> configs) {
        configure(new KetsieConfig(configs));
    }

    public void configure(KetsieConfig config) {
        this.config = config;
    }

    public void init() {
        Map<String, Object> configs = config.originals();
        String bootstrapServers = (String) configs.get(KafkaCacheConfig.KAFKACACHE_BOOTSTRAP_SERVERS_CONFIG);
        String groupId = (String) configs.getOrDefault(KafkaCacheConfig.KAFKACACHE_GROUP_ID_CONFIG, "ketsie-1");
        if (bootstrapServers != null) {
            String topic = "_commits";
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
        if (bootstrapServers != null) {
            String topic = "_timestamps";
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
        if (bootstrapServers != null) {
            String topic = "_leases";
            configs.put(KafkaCacheConfig.KAFKACACHE_TOPIC_CONFIG, topic);
            configs.put(KafkaCacheConfig.KAFKACACHE_GROUP_ID_CONFIG, groupId);
            configs.put(KafkaCacheConfig.KAFKACACHE_CLIENT_ID_CONFIG, groupId + "-" + topic);
            leases = new KafkaCache<>(
                new KafkaCacheConfig(configs), Serdes.Long(), new KafkaLeaseSerde(), null, new InMemoryCache<>());
        } else {
            leases = new InMemoryCache<>();
        }
        leases = Caches.concurrentCache(leases);
        leases.init();
        Cache<byte[], VersionedValues> cache;
        if (bootstrapServers != null) {
            String topic = "_ketsie";
            configs.put(KafkaCacheConfig.KAFKACACHE_TOPIC_CONFIG, topic);
            configs.put(KafkaCacheConfig.KAFKACACHE_GROUP_ID_CONFIG, groupId);
            configs.put(KafkaCacheConfig.KAFKACACHE_CLIENT_ID_CONFIG, groupId + "-" + topic);
            Comparator<byte[]> cmp = VersionedCache.BYTES_COMPARATOR;
            cache = new KafkaCache<>(
                new KafkaCacheConfig(configs), Serdes.ByteArray(), new KafkaValueSerde(), null, topic, cmp);
        } else {
            cache = new InMemoryCache<>();
        }
        cache.init();
        txCache = new TxVersionedCache(new VersionedCache("ketsie", cache));

        CommitTable commitTable = new KetsieCommitTable(commits);
        TimestampStorage timestampStorage = new KetsieTimestampStorage(timestamps);
        transactionManager = KetsieTransactionManager.newInstance(commitTable, timestampStorage);
        leaseManager = new KetsieLeaseManager(txCache, leases);

        boolean isInitialized = initialized.compareAndSet(false, true);
        if (!isInitialized) {
            throw new IllegalStateException("Illegal state while initializing engine. Engine "
                + "was already initialized");
        }
    }

    public boolean isInitialized() {
        return initialized.get();
    }

    public void sync() {
        commits.sync();
        timestamps.sync();
    }

    public TxVersionedCache getTxCache() {
        return txCache;
    }

    public KetsieTransactionManager getTxManager() {
        return transactionManager;
    }

    public KetsieLeaseManager getLeaseManager() {
        return leaseManager;
    }

    @Override
    public void close() throws IOException {
        transactionManager.close();
        timestamps.close();
        commits.close();
    }

    @SuppressWarnings("unchecked")
    public static <T> T getConfiguredInstance(String className, Map<String, ?> configs) {
        try {
            Class<T> cls = (Class<T>) Class.forName(className);
            if (cls == null) {
                return null;
            }
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

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

package io.kcache.keta.version;

import com.google.common.primitives.SignedBytes;
import io.kcache.keta.transaction.client.KetaTransactionManager;
import io.kcache.Cache;
import io.kcache.KeyValue;
import io.kcache.KeyValueIterator;
import io.kcache.utils.InMemoryCache;
import io.kcache.utils.Streams;
import org.apache.omid.transaction.TransactionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableMap;
import java.util.stream.Stream;

import static io.kcache.keta.version.TxVersionedCache.INVALID_TX;
import static io.kcache.keta.version.TxVersionedCache.PENDING_TX;

public class VersionedCache implements Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(VersionedCache.class);

    public static final Comparator<byte[]> BYTES_COMPARATOR = SignedBytes.lexicographicalComparator();
    public static final long NO_LEASE = 0L;

    private static final byte[] EMPTY_VALUE = new byte[0];

    private final String name;
    private final Cache<byte[], VersionedValues> cache;

    public VersionedCache(String name) {
        this(name, new InMemoryCache<>(BYTES_COMPARATOR));
    }

    public VersionedCache(String name,
                          Cache<byte[], VersionedValues> cache) {
        this.name = name;
        this.cache = cache;
    }

    public String getName() {
        return name;
    }

    public boolean keysEqual(byte[] key1, byte[] key2) {
        return Arrays.equals(key1, key2);
    }

    public boolean valuesEqual(byte[] value1, byte[] value2) {
        return Arrays.equals(value1, value2);
    }

    public VersionedValue get(byte[] key, long version) {
        VersionedValues rowData = cache.get(key);
        return rowData != null ? rowData.getValues().get(version) : null;
    }

    public List<VersionedValue> get(byte[] key, long minVersion, long maxVersion) {
        VersionedValues rowData = cache.get(key);
        return rowData != null ? getAll(rowData, minVersion, maxVersion) : Collections.emptyList();
    }

    public static List<VersionedValue> getAll(VersionedValues rowdata, long minVersion, long maxVersion) {
        List<VersionedValue> all = new ArrayList<>(rowdata.getValues().subMap(minVersion, true, maxVersion, true)
            .descendingMap()
            .values());
        return all;
    }

    public void put(int generationId, byte[] key, long version, byte[] value, long lease) {
        VersionedValues rowData = cache.getOrDefault(key, new VersionedValues(generationId));
        rowData.getValues().put(version, new VersionedValue(version, PENDING_TX, false, value, lease));
        garbageCollect(rowData.getValues());
        cache.put(key, rowData);
    }

    public boolean setCommit(int generationId, byte[] key, long version, long commit) {
        VersionedValues rowData = cache.getOrDefault(key, new VersionedValues(generationId));
        VersionedValue value = rowData.getValues().get(version);
        if (value == null) {
            return false;
        }
        if (commit == INVALID_TX) {
            rowData.getValues().remove(version);
        } else {
            rowData.getValues().put(version,
                new VersionedValue(version, commit, value.isDeleted(), value.getValue(), value.getLease()));
        }
        garbageCollect(rowData.getValues());
        cache.put(key, rowData);
        return true;
    }

    public void remove(int generationId, byte[] key, long version) {
        VersionedValues rowData = cache.getOrDefault(key, new VersionedValues(generationId));
        rowData.getValues().put(version, new VersionedValue(version, PENDING_TX, true, EMPTY_VALUE, NO_LEASE));
        garbageCollect(rowData.getValues());
        cache.put(key, rowData);
    }

    private void garbageCollect(NavigableMap<Long, VersionedValue> rowData) {
        // Discard all entries strictly older than the low water mark except the most recent
        try {
            KetaTransactionManager txManager = KetaTransactionManager.getInstance();
            if (txManager != null) {  // allow null for tests
                long lowWaterMark = txManager.getLowWatermark();
                List<Long> oldVersions = new ArrayList<>(rowData.headMap(lowWaterMark).keySet());
                if (oldVersions.size() > 1) {
                    for (int i = 0; i < oldVersions.size() - 1; i++) {
                        rowData.remove(oldVersions.get(i));
                    }
                }
            }
        } catch (TransactionException e) {
            throw new RuntimeException(e);
        }
    }

    public VersionedCache subCache(byte[] from, boolean fromInclusive, byte[] to, boolean toInclusive) {
        return new VersionedCache(name, cache.subCache(from, fromInclusive, to, toInclusive));
    }

    public KeyValueIterator<byte[], List<VersionedValue>> range(
        byte[] from, boolean fromInclusive, byte[] to, boolean toInclusive, boolean descending, long minVersion, long maxVersion) {
        KeyValueIterator<byte[], VersionedValues> iter;
        if (descending) {
            iter = cache.descendingCache().range(to, toInclusive, from, fromInclusive);
        } else {
            iter = cache.range(from, fromInclusive, to, toInclusive);
        }
        return new VersionedKeyValueIterator(iter, minVersion, maxVersion);
    }

    public KeyValueIterator<byte[], List<VersionedValue>> all(long minVersion, long maxVersion) {
        return new VersionedKeyValueIterator(cache.all(), minVersion, maxVersion);
    }

    public void flush() {
        cache.flush();
    }

    public void close() throws IOException {
        cache.close();
    }

    private static class VersionedKeyValueIterator implements KeyValueIterator<byte[], List<VersionedValue>> {
        private final KeyValueIterator<byte[], VersionedValues> rawIterator;
        private final Iterator<KeyValue<byte[], List<VersionedValue>>> iterator;

        VersionedKeyValueIterator(
            KeyValueIterator<byte[], VersionedValues> iter,
            long minVersion, long maxVersion) {
            this.rawIterator = iter;
            this.iterator = Streams.<KeyValue<byte[], VersionedValues>>streamOf(iter)
                .flatMap(kv -> {
                    List<VersionedValue> values = getAll(kv.value, minVersion, maxVersion);
                    return values.isEmpty() ? Stream.empty() : Stream.of(new KeyValue<>(kv.key, values));
                })
                .iterator();
        }

        public final boolean hasNext() {
            return iterator.hasNext();
        }

        public final KeyValue<byte[], List<VersionedValue>> next() {
            return iterator.next();
        }

        public final void remove() {
            throw new UnsupportedOperationException();
        }

        public final void close() {
            rawIterator.close();
        }
    }
}

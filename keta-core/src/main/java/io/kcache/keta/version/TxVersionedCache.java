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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.util.concurrent.Striped;
import io.kcache.keta.transaction.client.KetaCellId;
import io.kcache.keta.transaction.client.KetaTransaction;
import io.kcache.keta.transaction.client.SnapshotFilter;
import io.kcache.keta.transaction.client.SnapshotFilterImpl;
import io.kcache.KeyValue;
import io.kcache.KeyValueIterator;
import io.kcache.utils.Streams;
import org.apache.kafka.common.utils.Bytes;
import org.apache.omid.committable.CommitTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.stream.Collectors;

public class TxVersionedCache implements Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(TxVersionedCache.class);

    public static final long INVALID_TX = CommitTable.INVALID_TRANSACTION_MARKER;
    public static final long PENDING_TX = 0L;

    private final VersionedCache cache;
    private final boolean conflictFree;
    private final SnapshotFilter snapshotFilter;

    private final transient Striped<ReadWriteLock> striped;

    public TxVersionedCache(VersionedCache cache) {
        this(cache, false);
    }

    public TxVersionedCache(VersionedCache cache, boolean conflictFree) {
        this.cache = cache;
        this.conflictFree = conflictFree;
        this.snapshotFilter = new SnapshotFilterImpl(cache);
        this.striped = Striped.readWriteLock(128);
    }

    public String getName() {
        return cache.getName();
    }

    @VisibleForTesting
    public int size() {
        try (KeyValueIterator<byte[], VersionedValue> iter = all()) {
            return Iterators.size(iter);
        }
    }

    @VisibleForTesting
    public boolean isEmpty() {
        return size() == 0;
    }

    public VersionedValue get(byte[] key) {
        Lock lock = striped.get(Bytes.wrap(key)).readLock();
        lock.lock();
        try {
            List<VersionedValue> values = getVersions(key);
            return values.size() > 0 ? values.get(0) : null;
        } finally {
            lock.unlock();
        }
    }

    public void put(byte[] key, byte[] value) {
        put(key, value, VersionedCache.NO_LEASE);
    }

    public void put(byte[] key, byte[] value, long lease) {
        Lock lock = striped.get(Bytes.wrap(key)).writeLock();
        lock.lock();
        try {
            KetaTransaction tx = KetaTransaction.currentTransaction();
            List<VersionedValue> values = getVersions(key);
            if (values.size() > 0) {
                throw new IllegalStateException("Primary key constraint violation: " + Arrays.toString(key));
            }
            addWriteSetElement(tx, new KetaCellId(cache, key, tx.getWriteTimestamp()));
            cache.put(tx.getGenerationId(), key, tx.getWriteTimestamp(), value, lease);
        } finally {
            lock.unlock();
        }
    }

    public boolean replace(byte[] key, byte[] oldValue, byte[] newValue) {
        return replace(key, oldValue, newValue, VersionedCache.NO_LEASE);
    }

    public boolean replace(byte[] key, byte[] oldValue, byte[] newValue, long lease) {
        return replace(key, oldValue, key, newValue, lease);
    }

    public boolean replace(byte[] oldKey, byte[] oldValue,
                           byte[] newKey, byte[] newValue, long lease) {
        Iterable<ReadWriteLock> locks = striped.bulkGet(ImmutableList.of(Bytes.wrap(oldKey), Bytes.wrap(newKey)));
        List<Lock> writeLocks = Streams.streamOf(locks)
            .map(ReadWriteLock::writeLock)
            .collect(Collectors.toList());
        writeLocks.forEach(Lock::lock);
        try {
            KetaTransaction tx = KetaTransaction.currentTransaction();
            // Ensure the value hasn't changed
            List<VersionedValue> oldValues = getVersions(oldKey);
            byte[] oldVersionedValue = oldValues.size() > 0 ? oldValues.get(0).getValue() : null;
            if (!(oldValue == null && oldVersionedValue == null) && !Arrays.equals(oldValue, oldVersionedValue)) {
                throw new IllegalStateException("Previous value has changed");
            }
            if (cache.keysEqual(oldKey, newKey)) {
                if (cache.valuesEqual(oldValue, newValue)) {
                    return false;
                } else {
                    addWriteSetElement(tx, new KetaCellId(cache, newKey, tx.getWriteTimestamp()));
                    cache.put(tx.getGenerationId(), newKey, tx.getWriteTimestamp(), newValue, lease);
                    return true;
                }
            } else {
                List<VersionedValue> newValues = getVersions(newKey);
                if (newValues.size() > 0) {
                    throw new IllegalStateException("Primary key constraint violation: " + Arrays.toString(newKey));
                }
                addWriteSetElement(tx, new KetaCellId(cache, oldKey, tx.getWriteTimestamp()));
                addWriteSetElement(tx, new KetaCellId(cache, newKey, tx.getWriteTimestamp()));
                cache.remove(tx.getGenerationId(), oldKey, tx.getWriteTimestamp());
                cache.put(tx.getGenerationId(), newKey, tx.getWriteTimestamp(), newValue, lease);
                return true;
            }
        } finally {
            writeLocks.forEach(Lock::unlock);
        }
    }

    public void remove(byte[] key) {
        Lock lock = striped.get(Bytes.wrap(key)).writeLock();
        lock.lock();
        try {
            KetaTransaction tx = KetaTransaction.currentTransaction();
            addWriteSetElement(tx, new KetaCellId(cache, key, tx.getWriteTimestamp()));
            cache.remove(tx.getGenerationId(), key, tx.getWriteTimestamp());
        } finally {
            lock.unlock();
        }
    }

    public void remove(List<byte[]> keys) {
        Iterable<ReadWriteLock> locks = striped.bulkGet(
            keys.stream()
                .map(Bytes::wrap)
                .collect(Collectors.toList())
        );
        List<Lock> writeLocks = Streams.streamOf(locks)
            .map(ReadWriteLock::writeLock)
            .collect(Collectors.toList());
        writeLocks.forEach(Lock::lock);
        try {
            KetaTransaction tx = KetaTransaction.currentTransaction();
            for (byte[] key : keys) {
                addWriteSetElement(tx, new KetaCellId(cache, key, tx.getWriteTimestamp()));
                cache.remove(tx.getGenerationId(), key, tx.getWriteTimestamp());
            }
        } finally {
            writeLocks.forEach(Lock::unlock);
        }
    }

    public TxVersionedCache subCache(byte[] from, boolean fromInclusive, byte[] to, boolean toInclusive) {
        return new TxVersionedCache(cache.subCache(from, fromInclusive, to, toInclusive));
    }

    public List<VersionedValue> getVersions(byte[] key) {
        KetaTransaction tx = KetaTransaction.currentTransaction();
        return filterDeleted(snapshotFilter.get(tx, key));
    }

    private static List<VersionedValue> filterDeleted(List<VersionedValue> values) {
        List<VersionedValue> filtered = new ArrayList<>(values.size());
        for (VersionedValue value : values) {
            if (value.isDeleted()) {
                break;
            }
            filtered.add(value);
        }
        return filtered;
    }

    public List<VersionedValue> getVersions(byte[] key, VersionedValues versionedValues, boolean includeDeleted) {
        KetaTransaction tx = KetaTransaction.currentTransaction();
        List<VersionedValue> versions = ((SnapshotFilterImpl)snapshotFilter).get(tx, key, versionedValues);
        return includeDeleted ? versions : filterDeleted(versions);
    }

    public KeyValueIterator<byte[], VersionedValue> range(
        byte[] from, boolean fromInclusive, byte[] to, boolean toInclusive) {
        return range(from, fromInclusive, to, toInclusive, false);
    }

    public KeyValueIterator<byte[], VersionedValue> range(
        byte[] from, boolean fromInclusive, byte[] to, boolean toInclusive, boolean descending) {
        KetaTransaction tx = KetaTransaction.currentTransaction();
        return new FlattenedKeyValueIterator(snapshotFilter.range(tx, from, fromInclusive, to, toInclusive, descending));
    }

    public KeyValueIterator<byte[], VersionedValue> all() {
        KetaTransaction tx = KetaTransaction.currentTransaction();
        return new FlattenedKeyValueIterator(snapshotFilter.all(tx));
    }

    private void addWriteSetElement(KetaTransaction transaction, KetaCellId cellId) {
        if (conflictFree) {
            transaction.addConflictFreeWriteSetElement(cellId);
        } else {
            transaction.addWriteSetElement(cellId);
        }
    }

    public void flush() {
        cache.flush();
    }

    public void close() throws IOException {
        cache.close();
    }

    private static class FlattenedKeyValueIterator implements KeyValueIterator<byte[], VersionedValue> {
        private final KeyValueIterator<byte[], List<VersionedValue>> rawIterator;
        private final Iterator<KeyValue<byte[], VersionedValue>> iterator;

        FlattenedKeyValueIterator(
            KeyValueIterator<byte[], List<VersionedValue>> iter) {
            this.rawIterator = iter;
            this.iterator = Streams.<KeyValue<byte[], List<VersionedValue>>>streamOf(iter)
                .flatMap(kv -> filterDeleted(kv.value).stream().map(value -> new KeyValue<>(kv.key, value)))
                .iterator();
        }

        public final boolean hasNext() {
            return iterator.hasNext();
        }

        public final KeyValue<byte[], VersionedValue> next() {
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

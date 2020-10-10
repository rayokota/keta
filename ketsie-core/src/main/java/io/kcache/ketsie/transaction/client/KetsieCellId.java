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
package io.kcache.ketsie.transaction.client;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import io.kcache.ketsie.version.VersionedCache;
import org.apache.omid.tso.client.CellId;

import java.util.Arrays;

import static com.google.common.base.Charsets.UTF_8;

public class KetsieCellId implements CellId {

    private final VersionedCache cache;
    private final byte[] key;
    private final long timestamp;

    public KetsieCellId(VersionedCache cache, byte[] key, long timestamp) {
        this.timestamp = timestamp;
        this.cache = cache;
        this.key = key;
    }

    public VersionedCache getCache() {
        return cache;
    }

    public byte[] getKey() {
        return key;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return cache.getName()
            + ":" + Arrays.toString(key)
            + ":" + timestamp;
    }

    @Override
    public long getCellId() {
        return getHasher()
            .putBytes(cache.getName().getBytes(UTF_8))
            .putBytes(key)
            .hash().asLong();
    }

    @Override
    public long getTableId() {
        return getHasher()
            .putBytes(cache.getName().getBytes(UTF_8))
            .hash().asLong();
    }

    @Override
    public long getRowId() {
        return getHasher()
            .putBytes(cache.getName().getBytes(UTF_8))
            .putBytes(key)
            .hash().asLong();
    }

    public static Hasher getHasher() {
        return Hashing.murmur3_128().newHasher();
    }
}

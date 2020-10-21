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

import java.util.Arrays;
import java.util.Objects;

import static io.kcache.keta.version.TxVersionedCache.PENDING_TX;
import static io.kcache.keta.version.VersionedCache.NO_LEASE;

public class VersionedValue {
    private final long version;
    private final long commit;
    private final long create;
    private final long sequence;
    private final boolean deleted;
    private final byte[] value;
    private final long lease;

    public VersionedValue(long version, long create, long sequence, byte[] value) {
        this.version = version;
        this.commit = PENDING_TX;
        this.create = create;
        this.sequence = sequence;
        this.deleted = false;
        this.value = value;
        this.lease = NO_LEASE;
    }

    public VersionedValue(long version, long commit, long create, long sequence, byte[] value, long lease) {
        this.version = version;
        this.commit = commit;
        this.create = create;
        this.sequence = sequence;
        this.deleted = false;
        this.value = value;
        this.lease = lease;
    }

    public VersionedValue(long version, long commit, long create, long sequence, boolean deleted, byte[] value, long lease) {
        this.version = version;
        this.commit = commit;
        this.create = create;
        this.sequence = sequence;
        this.deleted = deleted;
        this.value = value;
        this.lease = lease;
    }

    public long getVersion() {
        return version;
    }

    public long getCommit() {
        return commit;
    }

    public long getCreate() {
        return create;
    }

    public long getSequence() {
        return sequence;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public byte[] getValue() {
        return value;
    }

    public long getLease() {
        return lease;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        VersionedValue that = (VersionedValue) o;
        return version == that.version
            && commit == that.commit
            && create == that.create
            && sequence == that.sequence
            && deleted == that.deleted
            && lease == that.lease
            && Arrays.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(version, commit, create, sequence, deleted, lease);
        result = 31 * result + Arrays.hashCode(value);
        return result;
    }
}


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

package io.kcache.ketsie.lease;

import org.apache.kafka.common.utils.Bytes;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public class LeaseKeys {
    private final Lease lease;
    private final Set<Bytes> keys;

    public LeaseKeys(Lease lease) {
        this(lease, new ConcurrentSkipListSet<>());
    }

    public LeaseKeys(Lease lease, Set<Bytes> keys) {
        this.lease = lease;
        this.keys = keys;
    }

    public Lease getLease() {
        return lease;
    }

    public long getId() {
        return lease.getId();
    }

    public long getTtl() {
        return lease.getTtl();
    }

    public long getExpiry() {
        return lease.getExpiry();
    }

    public Set<Bytes> getKeys() {
        return keys;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LeaseKeys leaseKeys = (LeaseKeys) o;
        return Objects.equals(lease, leaseKeys.lease)
            && Objects.equals(keys, leaseKeys.keys);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lease, keys);
    }
}


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

import java.util.Objects;

public class Lease {
    private final long id;
    private final long ttl;
    private final long expiry;

    public Lease(long id, long ttl, long expiry) {
        this.id = id;
        this.ttl = ttl;
        this.expiry = expiry;
    }

    public long getId() {
        return id;
    }

    public long getTtl() {
        return ttl;
    }

    public long getExpiry() {
        return expiry;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Lease lease = (Lease) o;
        return id == lease.id
            && ttl == lease.ttl
            && expiry == lease.expiry;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, ttl, expiry);
    }
}


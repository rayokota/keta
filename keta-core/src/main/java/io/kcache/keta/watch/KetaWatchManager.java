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
package io.kcache.keta.watch;

import io.kcache.Cache;
import io.kcache.keta.KetaEngine;
import io.kcache.keta.lease.Lease;
import io.kcache.keta.lease.LeaseKeys;
import io.kcache.keta.utils.IntervalTree;
import io.kcache.keta.version.TxVersionedCache;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.apache.kafka.common.utils.Bytes;
import org.apache.omid.transaction.RollbackException;
import org.apache.omid.transaction.Transaction;
import org.apache.omid.transaction.TransactionException;
import org.apache.omid.transaction.TransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class KetaWatchManager {

    private static final Logger LOG = LoggerFactory.getLogger(KetaWatchManager.class);

    private final Map<Bytes, Set<Watch>> keyWatchers;
    private final IntervalTree<Bytes, Set<Watch>> ranges;
    private final Set<Watch> watchers;

    public KetaWatchManager() {
        this.keyWatchers = new HashMap<>();
        this.ranges = new IntervalTree<>();
        this.watchers = new HashSet<>();
    }

    public void add(Watch watch) {
    }

    public Set<Watch> getWatches(byte[] key) {
        return null;

    }

}

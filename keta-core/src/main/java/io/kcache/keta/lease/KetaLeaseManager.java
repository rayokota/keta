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
package io.kcache.keta.lease;

import io.kcache.Cache;
import io.kcache.keta.KetaEngine;
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

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class KetaLeaseManager {

    private static final Logger LOG = LoggerFactory.getLogger(KetaLeaseManager.class);

    private final TxVersionedCache txVersionedCache;
    private final Cache<Long, Lease> cache;
    private final ExpiringMap<Long, LeaseKeys> expiringMap;

    public KetaLeaseManager(TxVersionedCache txVersionedCache, Cache<Long, Lease> cache) {
        this.txVersionedCache = txVersionedCache;
        this.cache = cache;
        this.expiringMap = ExpiringMap.<Long, LeaseKeys>builder()
            .expirationPolicy(ExpirationPolicy.CREATED)
            .variableExpiration()
            .expirationListener((l, lk) -> revoke((LeaseKeys) lk))
            .build();
    }

    public LeaseKeys grant(Lease lease) {
        long id = lease.getId();
        while (id == 0) {
            long newId = ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
            if (!cache.containsKey(newId)) {
                lease = new Lease(newId, lease.getTtl(), lease.getExpiry());
                id = newId;
            }
        }
        if (cache.containsKey(id)) {
            throw new IllegalArgumentException("Lease with id " + id + " already exists");
        }
        cache.put(lease.getId(), lease);
        LeaseKeys lk = new LeaseKeys(lease);
        long duration = lease.getExpiry() - System.currentTimeMillis();
        if (duration > 0) {
            expiringMap.put(lease.getId(), lk, duration, TimeUnit.MILLISECONDS);
        }
        return lk;
    }

    public LeaseKeys get(long id) {
        LeaseKeys lk = expiringMap.get(id);
        if (lk == null) {
            throw new IllegalArgumentException("No lease with id " + id);
        }
        return lk;
    }

    public LeaseKeys revoke(long id) {
        LeaseKeys lk = expiringMap.remove(id);
        if (lk == null) {
            throw new IllegalArgumentException("No lease with id " + id);
        }
        revoke(lk);
        return lk;
    }

    private void revoke(LeaseKeys lk) {
        if (!KetaEngine.getInstance().isLeader()) {
            return;
        }
        TransactionManager txMgr = KetaEngine.getInstance().getTxManager();
        Transaction tx = null;
        try {
            tx = txMgr.begin();
            for (Bytes key : lk.getKeys()) {
                txVersionedCache.remove(key.get());
            }
            txMgr.commit(tx);
            cache.remove(lk.getId());
        } catch (TransactionException | RollbackException e) {
            if (tx != null) {
                try {
                    txMgr.rollback(tx);
                } catch (TransactionException te) {
                    // ignore
                }
            }
            throw new IllegalStateException(e);
        }
    }

    public LeaseKeys renew(long id) {
        LeaseKeys lk = expiringMap.remove(id);
        if (lk == null) {
            throw new IllegalArgumentException("No lease with id " + id);
        }
        Lease oldLease = lk.getLease();
        Lease newLease = new Lease(id, oldLease.getTtl(), System.currentTimeMillis() + oldLease.getTtl() * 1000);
        cache.put(id, newLease);
        LeaseKeys newlk = new LeaseKeys(newLease, lk.getKeys());
        long duration = newLease.getExpiry() - System.currentTimeMillis();
        if (duration > 0) {
            expiringMap.put(id, newlk, duration, TimeUnit.MILLISECONDS);
        }
        return newlk;
    }
}

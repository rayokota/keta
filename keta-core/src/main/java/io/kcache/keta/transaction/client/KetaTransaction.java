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
package io.kcache.keta.transaction.client;

import io.kcache.keta.version.VersionedCache;
import org.apache.omid.transaction.AbstractTransaction;
import org.apache.omid.transaction.AbstractTransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static io.kcache.keta.version.TxVersionedCache.INVALID_TX;

public class KetaTransaction extends AbstractTransaction<KetaCellId> {

    private static final Logger LOG = LoggerFactory.getLogger(KetaTransaction.class);

    private static final ThreadLocal<KetaTransaction> currentTransaction = new ThreadLocal<>();

    public static KetaTransaction currentTransaction() {
        KetaTransaction transaction = currentTransaction.get();
        if (transaction == null) {
            throw new IllegalStateException("No current transaction");
        } else if (transaction.getStatus() == Status.ROLLEDBACK) {
            throw new IllegalStateException("Transaction was already " + transaction.getStatus());
        }
        return transaction;
    }

    public static void setCurrentTransaction(KetaTransaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("No current transaction");
        } else if (transaction.getStatus() != Status.RUNNING) {
            throw new IllegalArgumentException("Transaction was already " + transaction.getStatus());
        }
        currentTransaction.set(transaction);
    }

    public KetaTransaction(long transactionId, long epoch, Set<KetaCellId> writeSet,
                           Set<KetaCellId> conflictFreeWriteSet, AbstractTransactionManager tm, boolean isLowLatency) {
        super(transactionId, epoch, writeSet, conflictFreeWriteSet, tm, isLowLatency);
    }

    public KetaTransaction(long transactionId, long epoch, Set<KetaCellId> writeSet,
                           Set<KetaCellId> conflictFreeWriteSet, AbstractTransactionManager tm,
                           long readTimestamp, long writeTimestamp, boolean isLowLatency) {
        super(transactionId, epoch, writeSet, conflictFreeWriteSet, tm, readTimestamp, writeTimestamp, isLowLatency);
    }

    public KetaTransaction(long transactionId, long readTimestamp, VisibilityLevel visibilityLevel, long epoch,
                           Set<KetaCellId> writeSet, Set<KetaCellId> conflictFreeWriteSet,
                           AbstractTransactionManager tm, boolean isLowLatency) {
        super(transactionId, readTimestamp, visibilityLevel, epoch, writeSet, conflictFreeWriteSet, tm, isLowLatency);
    }

    public int getGenerationId() {
        return ((KetaTransactionManager) getTransactionManager()).getGenerationId();
    }

    @Override
    public void cleanup() {
        try {
            Map<String, VersionedCache> caches = new HashMap<>();

            for (final KetaCellId cell : getWriteSet()) {
                VersionedCache cache = cell.getCache();
                caches.put(cache.getName(), cache);
                cache.setCommit(getGenerationId(), cell.getKey(), cell.getTimestamp(), INVALID_TX);
            }

            for (final KetaCellId cell : getConflictFreeWriteSet()) {
                VersionedCache cache = cell.getCache();
                caches.put(cache.getName(), cache);
                cache.setCommit(getGenerationId(), cell.getKey(), cell.getTimestamp(), INVALID_TX);
            }

            for (VersionedCache cache : caches.values()) {
                cache.flush();
            }
        } catch (Exception e) {
            LOG.warn("Failed cleanup for Tx {}. This issue has been ignored", getTransactionId(), e);
        }
    }
}

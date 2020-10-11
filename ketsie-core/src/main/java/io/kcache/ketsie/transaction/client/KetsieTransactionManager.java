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

import com.google.common.base.Optional;
import io.kcache.ketsie.transaction.InMemoryCommitTable;
import io.kcache.ketsie.transaction.InMemoryTimestampStorage;
import io.kcache.ketsie.transaction.KetsieTimestampClient;
import io.kcache.ketsie.version.VersionedCache;
import io.kcache.ketsie.version.VersionedValue;
import org.apache.omid.committable.CommitTable;
import org.apache.omid.metrics.MetricsRegistry;
import org.apache.omid.metrics.NullMetricsProvider;
import org.apache.omid.timestamp.storage.TimestampStorage;
import org.apache.omid.transaction.AbstractTransaction;
import org.apache.omid.transaction.AbstractTransactionManager;
import org.apache.omid.transaction.AbstractTransactionManagerShim;
import org.apache.omid.transaction.CommitTimestampLocator;
import org.apache.omid.transaction.PostCommitActions;
import org.apache.omid.transaction.TransactionException;
import org.apache.omid.transaction.TransactionManagerException;
import org.apache.omid.tso.RuntimeExceptionPanicker;
import org.apache.omid.tso.TimestampOracle;
import org.apache.omid.tso.TimestampOracleImpl;
import org.apache.omid.tso.client.CellId;
import org.apache.omid.tso.client.TSOProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class KetsieTransactionManager extends AbstractTransactionManagerShim {

    private static final Logger LOG = LoggerFactory.getLogger(KetsieTransactionManager.class);

    private static class KetsieTransactionFactory implements TransactionFactory<KetsieCellId> {
        @Override
        public KetsieTransaction createTransaction(long transactionId, long epoch, AbstractTransactionManager tm) {
            return new KetsieTransaction(transactionId, epoch, new HashSet<>(), new HashSet<>(),
                tm, tm.isLowLatency());
        }
    }

    private volatile int generationId = -1;

    private static KetsieTransactionManager INSTANCE;

    public static KetsieTransactionManager getInstance() {
        return INSTANCE;
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Construction
    // ----------------------------------------------------------------------------------------------------------------

    // visible for testing
    public static KetsieTransactionManager newInstance() {
        return newInstance(new InMemoryCommitTable(), new InMemoryTimestampStorage());
    }

    public static KetsieTransactionManager newInstance(CommitTable commitTable,
                                                       TimestampStorage timestampStorage) {
        try {
            MetricsRegistry metricsRegistry = new NullMetricsProvider();
            TimestampOracle timestampOracle = new TimestampOracleImpl(
                metricsRegistry, timestampStorage, new RuntimeExceptionPanicker());
            timestampOracle.initialize();
            PostCommitActions postCommitter = new KetsieSyncPostCommitter(commitTable.getClient());
            return newInstance(commitTable, timestampOracle, postCommitter);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static KetsieTransactionManager newInstance(CommitTable commitTable,
                                                       TimestampOracle timestampOracle,
                                                       PostCommitActions postCommitter) {
        try {
            MetricsRegistry metricsRegistry = new NullMetricsProvider();
            CommitTable.Client commitTableClient = commitTable.getClient();
            CommitTable.Writer commitTableWriter = commitTable.getWriter();
            TSOProtocol tsoClient = new KetsieTimestampClient(timestampOracle, commitTableWriter);

            INSTANCE = new KetsieTransactionManager(
                metricsRegistry,
                postCommitter,
                tsoClient,
                commitTableClient,
                commitTableWriter,
                new KetsieTransactionFactory());
            return INSTANCE;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private KetsieTransactionManager(MetricsRegistry metricsRegistry,
                                     PostCommitActions postCommitter,
                                     TSOProtocol tsoClient,
                                     CommitTable.Client commitTableClient,
                                     CommitTable.Writer commitTableWriter,
                                     KetsieTransactionFactory transactionFactory) {
        super(metricsRegistry,
            postCommitter,
            tsoClient,
            commitTableClient,
            commitTableWriter,
            transactionFactory);
    }

    public int getGenerationId() {
        return generationId;
    }

    public void setGenerationId(int generationId) {
        this.generationId = generationId;
    }

    // ----------------------------------------------------------------------------------------------------------------
    // AbstractTransactionManager overwritten methods
    // ----------------------------------------------------------------------------------------------------------------
    @Override
    public void postBegin(AbstractTransaction<? extends CellId> transaction) throws TransactionManagerException {
        KetsieTransaction.setCurrentTransaction(((KetsieTransaction) transaction));
    }

    @Override
    public void closeResources() throws IOException {
    }

    @Override
    public long getHashForTable(byte[] tableName) {
        return KetsieCellId.getHasher().putBytes(tableName).hash().asLong();
    }

    public long getLowWatermark() throws TransactionException {
        try {
            return commitTableClient.readLowWatermark().get();
        } catch (ExecutionException ee) {
            throw new TransactionException("Error reading low watermark", ee.getCause());
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new TransactionException("Interrupted reading low watermark", ie);
        }
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Helper methods
    // ----------------------------------------------------------------------------------------------------------------

    public static KetsieTransaction enforceKetsieTransactionAsParam(AbstractTransaction<? extends CellId> tx) {

        if (tx instanceof KetsieTransaction) {
            return (KetsieTransaction) tx;
        } else {
            throw new IllegalArgumentException(
                "The transaction object passed is not an instance of KetsieTransaction");
        }
    }

    public static class CommitTimestampLocatorImpl implements CommitTimestampLocator {

        private final KetsieCellId cellId;
        private final Map<Long, Long> commitCache;
        private final VersionedCache versionedCache;

        public CommitTimestampLocatorImpl(KetsieCellId cellId, Map<Long, Long> commitCache, VersionedCache versionedCache) {
            this.cellId = cellId;
            this.commitCache = commitCache;
            this.versionedCache = versionedCache;
        }

        @Override
        public Optional<Long> readCommitTimestampFromCache(long startTimestamp) {
            return Optional.fromNullable(commitCache.get(startTimestamp));
        }

        @Override
        public Optional<Long> readCommitTimestampFromShadowCell(long startTimestamp) throws IOException {
            VersionedValue value = versionedCache.get(cellId.getKey(), startTimestamp);
            if (value == null) {
                return Optional.absent();
            }
            long commit = value.getCommit();
            return Optional.fromNullable(commit > 0 ? commit : null);
        }
    }
}

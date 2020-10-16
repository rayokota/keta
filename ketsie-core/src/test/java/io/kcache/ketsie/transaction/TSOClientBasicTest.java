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
package io.kcache.ketsie.transaction;

import com.google.common.collect.Sets;
import io.kcache.utils.InMemoryCache;
import org.apache.omid.committable.CommitTable;
import org.apache.omid.metrics.NullMetricsProvider;
import org.apache.omid.timestamp.storage.TimestampStorage;
import org.apache.omid.tso.RuntimeExceptionPanicker;
import org.apache.omid.tso.TimestampOracle;
import org.apache.omid.tso.TimestampOracleImpl;
import org.apache.omid.tso.client.AbortException;
import org.apache.omid.tso.client.CellId;
import org.apache.omid.tso.client.TSOProtocol;
import org.apache.omid.tso.util.DummyCellIdImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TSOClientBasicTest {

    private static final Logger LOG = LoggerFactory.getLogger(io.kcache.ketsie.transaction.TSOClientBasicTest.class);

    // Cells for tests
    private final static CellId c1 = new DummyCellIdImpl(0xdeadbeefL);
    private final static CellId c2 = new DummyCellIdImpl(0xfeedcafeL);

    private CommitTable commitTable;
    private CommitTable.Client commitTableClient;
    private TSOProtocol tsoClient;
    private TSOProtocol justAnotherTSOClient;

    @BeforeEach
    public void setUp() throws Exception {
        commitTable = new KetsieCommitTable(new InMemoryCache<>());
        commitTableClient = commitTable.getClient();
        TimestampStorage timestampStorage = new KetsieTimestampStorage(new InMemoryCache<>());
        TimestampOracle timestampOracle = new TimestampOracleImpl(
            new NullMetricsProvider(), timestampStorage, new RuntimeExceptionPanicker());
        timestampOracle.initialize();
        tsoClient = new KetsieTimestampClient(timestampOracle, commitTable.getWriter());
        justAnotherTSOClient = new KetsieTimestampClient(timestampOracle, commitTable.getWriter());
    }

    @Test
    public void testTimestampsOrderingGrowMonotonically() throws Exception {
        long referenceTimestamp;
        long startTsTx1 = tsoClient.getNewStartTimestamp().get();
        referenceTimestamp = startTsTx1;

        long startTsTx2 = tsoClient.getNewStartTimestamp().get();
        referenceTimestamp += CommitTable.MAX_CHECKPOINTS_PER_TXN;
        assertTrue(startTsTx2 >= referenceTimestamp, "Should grow monotonically");
        assertTrue(startTsTx2 > startTsTx1, "Two timestamps obtained consecutively should grow");

        long commitTsTx2 = tsoClient.commit(startTsTx2, Sets.newHashSet(c1)).get();
        referenceTimestamp += CommitTable.MAX_CHECKPOINTS_PER_TXN;
        assertTrue(commitTsTx2 >= referenceTimestamp, "Should grow monotonically");

        long commitTsTx1 = tsoClient.commit(startTsTx1, Sets.newHashSet(c2)).get();
        referenceTimestamp += CommitTable.MAX_CHECKPOINTS_PER_TXN;
        assertTrue(commitTsTx1 >= referenceTimestamp, "Should grow monotonically");

        long startTsTx3 = tsoClient.getNewStartTimestamp().get();
        referenceTimestamp += CommitTable.MAX_CHECKPOINTS_PER_TXN;
        assertTrue(startTsTx3 >= referenceTimestamp, "Should grow monotonically");
    }

    @Test
    public void testSimpleTransactionWithNoWriteSetCanCommit() throws Exception {
        long startTsTx1 = tsoClient.getNewStartTimestamp().get();
        long commitTsTx1 = tsoClient.commit(startTsTx1, Sets.<CellId>newHashSet()).get();
        assertTrue(commitTsTx1 > startTsTx1);
    }

    @Test
    public void testTransactionWithMassiveWriteSetCanCommit() throws Exception {
        long startTs = tsoClient.getNewStartTimestamp().get();

        Set<CellId> cells = new HashSet<>();
        for (int i = 0; i < 1_000_000; i++) {
            cells.add(new DummyCellIdImpl(i));
        }

        long commitTs = tsoClient.commit(startTs, cells).get();
        assertTrue(commitTs > startTs, "Commit TS should be higher than Start TS");
    }

    @Test
    public void testMultipleSerialCommitsDoNotConflict() throws Exception {
        long startTsTx1 = tsoClient.getNewStartTimestamp().get();
        long commitTsTx1 = tsoClient.commit(startTsTx1, Sets.newHashSet(c1)).get();
        assertTrue(commitTsTx1 > startTsTx1, "Commit TS must be greater than Start TS");

        long startTsTx2 = tsoClient.getNewStartTimestamp().get();
        assertTrue(startTsTx2 > commitTsTx1, "TS should grow monotonically");

        long commitTsTx2 = tsoClient.commit(startTsTx2, Sets.newHashSet(c1, c2)).get();
        assertTrue(commitTsTx2 > startTsTx2, "Commit TS must be greater than Start TS");

        long startTsTx3 = tsoClient.getNewStartTimestamp().get();
        long commitTsTx3 = tsoClient.commit(startTsTx3, Sets.newHashSet(c2)).get();
        assertTrue(commitTsTx3 > startTsTx3, "Commit TS must be greater than Start TS");
    }

    @Test
    public void testCommitWritesToCommitTable() throws Exception {

        long startTsForTx1 = tsoClient.getNewStartTimestamp().get();
        long startTsForTx2 = tsoClient.getNewStartTimestamp().get();
        assertTrue(startTsForTx2 > startTsForTx1, "Start TS should grow");

        if (!tsoClient.isLowLatency())
            assertFalse(commitTableClient.getCommitTimestamp(startTsForTx1).get().isPresent(),
                "Commit TS for TX1 shouldn't appear in Commit Table");

        long commitTsForTx1 = tsoClient.commit(startTsForTx1, Sets.newHashSet(c1)).get();
        assertTrue(commitTsForTx1 > startTsForTx1, "Commit TS should be higher than Start TS for the same tx");

        if (!tsoClient.isLowLatency()) {
            Long commitTs1InCommitTable = commitTableClient.getCommitTimestamp(startTsForTx1).get().get().getValue();
            assertNotNull(commitTs1InCommitTable, "Tx is committed, should return as such from Commit Table");
            assertEquals(commitTsForTx1, (long) commitTs1InCommitTable,
                "getCommitTimestamp() & commit() should report same Commit TS value for same tx");
            assertTrue(commitTs1InCommitTable > startTsForTx2, "Commit TS should be higher than tx's Start TS");
        } else {
            assertTrue(commitTsForTx1 > startTsForTx2, "Commit TS should be higher than tx's Start TS");
        }
    }

    @Test
    public void testTwoConcurrentTxWithOverlappingWritesetsHaveConflicts() throws Exception {
        long startTsTx1 = tsoClient.getNewStartTimestamp().get();
        long startTsTx2 = tsoClient.getNewStartTimestamp().get();
        assertTrue(startTsTx2 > startTsTx1, "Second TX should have higher TS");

        long commitTsTx1 = tsoClient.commit(startTsTx1, Sets.newHashSet(c1)).get();
        assertTrue(commitTsTx1 > startTsTx1, "Commit TS must be higher than Start TS for the same tx");

        try {
            tsoClient.commit(startTsTx2, Sets.newHashSet(c1, c2)).get();
            Assertions.fail("Second TX should fail on commit");
        } catch (ExecutionException ee) {
            assertEquals(AbortException.class, ee.getCause().getClass(), "Should have aborted");
        }
    }

    @Test
    public void testTransactionStartedBeforeFenceAborts() throws Exception {

        long startTsTx1 = tsoClient.getNewStartTimestamp().get();

        long fenceID = tsoClient.getFence(c1.getTableId()).get();

        assertTrue(fenceID > startTsTx1, "Fence ID should be higher thank Tx1ID");

        try {
            tsoClient.commit(startTsTx1, Sets.newHashSet(c1, c2)).get();
            Assertions.fail("TX should fail on commit");
        } catch (ExecutionException ee) {
            assertEquals(AbortException.class, ee.getCause().getClass(), "Should have aborted");
        }
    }

    @Test
    public void testTransactionStartedBeforeNonOverlapFenceCommits() throws Exception {

        long startTsTx1 = tsoClient.getNewStartTimestamp().get();

        tsoClient.getFence(7).get();

        try {
            tsoClient.commit(startTsTx1, Sets.newHashSet(c1, c2)).get();
        } catch (ExecutionException ee) {
            Assertions.fail("TX should successfully commit");
        }
    }

    @Test
    public void testTransactionStartedAfterFenceCommits() throws Exception {

        tsoClient.getFence(c1.getTableId()).get();

        long startTsTx1 = tsoClient.getNewStartTimestamp().get();

        try {
            tsoClient.commit(startTsTx1, Sets.newHashSet(c1, c2)).get();
        } catch (ExecutionException ee) {
            Assertions.fail("TX should successfully commit");
        }
    }

    @Test
    public void testConflictsAndMonotonicallyTimestampGrowthWithTwoDifferentTSOClients() throws Exception {
        long startTsTx1Client1 = tsoClient.getNewStartTimestamp().get();
        long startTsTx2Client1 = tsoClient.getNewStartTimestamp().get();
        long startTsTx3Client1 = tsoClient.getNewStartTimestamp().get();

        Long commitTSTx1 = tsoClient.commit(startTsTx1Client1, Sets.newHashSet(c1)).get();
        try {
            tsoClient.commit(startTsTx3Client1, Sets.newHashSet(c1, c2)).get();
            Assertions.fail("Second commit should fail as conflicts with the previous concurrent one");
        } catch (ExecutionException ee) {
            assertEquals(AbortException.class, ee.getCause().getClass(), "Should have aborted");
        }
        long startTsTx4Client2 = justAnotherTSOClient.getNewStartTimestamp().get();

        assertFalse(commitTableClient.getCommitTimestamp(startTsTx3Client1).get().isPresent(), "Tx3 didn't commit");
        if (!tsoClient.isLowLatency())
            commitTSTx1 = commitTableClient.getCommitTimestamp(startTsTx1Client1).get().get().getValue();
        assertTrue(commitTSTx1 > startTsTx2Client1, "Tx1 committed after Tx2 started");
        assertTrue(commitTSTx1 < startTsTx4Client2, "Tx1 committed before Tx4 started on the other TSO client");
    }
}

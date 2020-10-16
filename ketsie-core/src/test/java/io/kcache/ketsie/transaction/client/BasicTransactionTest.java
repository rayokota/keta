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

import io.kcache.ketsie.version.TxVersionedCache;
import io.kcache.ketsie.version.VersionedCache;
import io.kcache.ketsie.version.VersionedValue;
import io.kcache.KeyValue;
import org.apache.omid.transaction.RollbackException;
import org.apache.omid.transaction.Transaction;
import org.apache.omid.transaction.TransactionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class BasicTransactionTest {

    private static final Logger LOG = LoggerFactory.getLogger(BasicTransactionTest.class);

    private static final String TEST_TABLE = "test-table";

    private byte[] rowId1 = "row1".getBytes();
    private byte[] rowId2 = "row2".getBytes();

    private byte[] dataValue1 = "testWrite-1".getBytes();
    private byte[] dataValue2 = "testWrite-2".getBytes();

    private TransactionManager tm;
    private TxVersionedCache versionedCache;

    @BeforeEach
    public void setUp() throws Exception {
        tm = KetsieTransactionManager.newInstance();
        versionedCache = new TxVersionedCache(new VersionedCache(TEST_TABLE));
    }

    @AfterEach
    public void tearDown() throws Exception {
        tm.close();
    }

    @Test
    public void testTimestampsOfTwoRowsInsertedAfterCommitOfSingleTransactionAreEquals() throws Exception {

        Transaction tx1 = tm.begin();

        KetsieTransaction.setCurrentTransaction((KetsieTransaction) tx1);
        versionedCache.put(rowId1, dataValue1);
        versionedCache.put(rowId2, dataValue2);

        tm.commit(tx1);

        Transaction tx2 = tm.begin();

        KetsieTransaction.setCurrentTransaction((KetsieTransaction) tx2);
        VersionedValue v1 = versionedCache.get(rowId1);
        VersionedValue v2 = versionedCache.get(rowId2);

        assertArrayEquals(dataValue1, v1.getValue());
        assertArrayEquals(dataValue2, v2.getValue());

        assertEquals(v1.getCommit(), v2.getCommit());
    }

    @Test
    public void runTestSimple() throws Exception {

        Transaction t1 = tm.begin();
        LOG.info("Transaction created " + t1);

        KetsieTransaction.setCurrentTransaction((KetsieTransaction) t1);
        versionedCache.put(rowId1, dataValue1);

        tm.commit(t1);

        Transaction tread = tm.begin();
        Transaction t2 = tm.begin();

        KetsieTransaction.setCurrentTransaction((KetsieTransaction) t2);
        versionedCache.replace(rowId1, dataValue1, dataValue2);

        tm.commit(t2);

        KetsieTransaction.setCurrentTransaction((KetsieTransaction) tread);
        VersionedValue v1 = versionedCache.get(rowId1);
        assertArrayEquals(dataValue1, v1.getValue());
    }

    @Test
    public void runTestManyVersions() throws Exception {

        Transaction t1 = tm.begin();
        LOG.info("Transaction created " + t1);

        KetsieTransaction.setCurrentTransaction((KetsieTransaction) t1);
        versionedCache.put(rowId1, dataValue1);

        tm.commit(t1);

        for (int i = 0; i < 5; ++i) {
            Transaction t2 = tm.begin();
            KetsieTransaction.setCurrentTransaction((KetsieTransaction) t2);
            versionedCache.replace(rowId1, dataValue1, dataValue2);
        }

        Transaction tread = tm.begin();

        KetsieTransaction.setCurrentTransaction((KetsieTransaction) tread);
        VersionedValue v1 = versionedCache.get(rowId1);
        assertArrayEquals(dataValue1, v1.getValue());
    }

    @Test
    public void runTestInterleave() throws Exception {

        Transaction t1 = tm.begin();
        LOG.info("Transaction created " + t1);

        KetsieTransaction.setCurrentTransaction((KetsieTransaction) t1);
        versionedCache.put(rowId1, dataValue1);

        tm.commit(t1);

        Transaction t2 = tm.begin();

        KetsieTransaction.setCurrentTransaction((KetsieTransaction) t2);
        versionedCache.replace(rowId1, dataValue1, dataValue2);

        Transaction tread = tm.begin();

        KetsieTransaction.setCurrentTransaction((KetsieTransaction) tread);
        VersionedValue v1 = versionedCache.get(rowId1);
        assertArrayEquals(dataValue1, v1.getValue());

        try {
            tm.commit(t2);
        } catch (RollbackException e) {
        }
    }

    @Test
    public void testSameCommitRaisesException() throws Exception {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            Transaction t1 = tm.begin();
            tm.commit(t1);
            tm.commit(t1);
        });
    }

    @Test
    public void testInterleavedScanReturnsTheRightSnapshotResults() throws Exception {

        byte[] startRow = ("row-to-scan" + 0).getBytes();
        byte[] stopRow = ("row-to-scan" + 9).getBytes();
        byte[] randomRow = ("row-to-scan" + 3).getBytes();

        // Add some data transactionally to have an initial state for the test
        Transaction tx1 = tm.begin();
        for (int i = 0; i < 10; i++) {
            byte[] row = ("row-to-scan" + i).getBytes();
            KetsieTransaction.setCurrentTransaction((KetsieTransaction) tx1);
            versionedCache.put(row, dataValue1);
        }
        tm.commit(tx1);

        // Start a second transaction -Tx2- modifying a random row and check that a concurrent transactional context
        // that scans the table, gets the proper snapshot with the stuff written by Tx1
        Transaction tx2 = tm.begin();
        KetsieTransaction.setCurrentTransaction((KetsieTransaction) tx2);
        versionedCache.replace(randomRow, dataValue1, dataValue2);

        Transaction scanTx = tm.begin(); // This is the concurrent transactional scanner
        KetsieTransaction.setCurrentTransaction((KetsieTransaction) scanTx);

        Iterator<KeyValue<byte[], VersionedValue>> iter = versionedCache.range(startRow, true, stopRow, true);
        while (iter.hasNext()) {
            KeyValue<byte[], VersionedValue> kv = iter.next();
            assertArrayEquals(dataValue1, kv.value.getValue());
        }

        // Commit the Tx2 and then check that under a new transactional context, the scanner gets the right snapshot,
        // which must include the row modified by Tx2
        tm.commit(tx2);

        int modifiedRows = 0;
        Transaction newScanTx = tm.begin();
        KetsieTransaction.setCurrentTransaction((KetsieTransaction) newScanTx);
        Iterator<KeyValue<byte[], VersionedValue>> newIter = versionedCache.range(startRow, true, stopRow, true);
        while (newIter.hasNext()) {
            KeyValue<byte[], VersionedValue> kv = newIter.next();
            if (Arrays.equals(dataValue2, kv.value.getValue())) {
                modifiedRows++;
            }
        }
        assertEquals(modifiedRows, 1, "Expected 1 row modified, but " + modifiedRows + " are.");

        // Finally, check that the Scanner Iterator does not implement the remove method
        newIter = versionedCache.range(startRow, true, stopRow, true);
        try {
            newIter.remove();
            fail();
        } catch (RuntimeException re) {
            // Expected
        }
    }

    @Test
    public void testInterleavedScanReturnsTheRightSnapshotResultsWhenATransactionAborts()
        throws Exception {

        byte[] startRow = ("row-to-scan" + 0).getBytes();
        byte[] stopRow = ("row-to-scan" + 9).getBytes();
        byte[] randomRow = ("row-to-scan" + 3).getBytes();

        // Add some data transactionally to have an initial state for the test
        Transaction tx1 = tm.begin();
        for (int i = 0; i < 10; i++) {
            byte[] row = ("row-to-scan" + i).getBytes();
            KetsieTransaction.setCurrentTransaction((KetsieTransaction) tx1);
            versionedCache.put(row, dataValue1);
        }
        tm.commit(tx1);

        // Start a second transaction modifying a random row and check that a transactional scanner in Tx2 gets the
        // right snapshot with the new value in the random row just written by Tx2
        Transaction tx2 = tm.begin();
        KetsieTransaction.setCurrentTransaction((KetsieTransaction) tx2);
        versionedCache.replace(randomRow, dataValue1, dataValue2);

        int modifiedRows = 0;
        KetsieTransaction.setCurrentTransaction((KetsieTransaction) tx2);
        Iterator<KeyValue<byte[], VersionedValue>> newIter = versionedCache.range(startRow, true, stopRow, true);
        while (newIter.hasNext()) {
            KeyValue<byte[], VersionedValue> kv = newIter.next();
            if (Arrays.equals(dataValue2, kv.value.getValue())) {
                modifiedRows++;
            }
        }
        assertEquals(modifiedRows, 1, "Expected 1 row modified, but " + modifiedRows + " are.");

        // Rollback the second transaction and then check that under a new transactional scanner we get the snapshot
        // that includes the only the initial rows put by Tx1
        tm.rollback(tx2);

        Transaction txScan = tm.begin(); // This is the concurrent transactional scanner
        KetsieTransaction.setCurrentTransaction((KetsieTransaction) txScan);

        Iterator<KeyValue<byte[], VersionedValue>> iter = versionedCache.range(startRow, true, stopRow, true);
        while (iter.hasNext()) {
            KeyValue<byte[], VersionedValue> kv = iter.next();
            assertArrayEquals(dataValue1, kv.value.getValue());
        }

        // Finally, check that the Scanner Iterator does not implement the remove method
        newIter = versionedCache.range(startRow, true, stopRow, true);
        try {
            newIter.remove();
            fail();
        } catch (RuntimeException re) {
            // Expected
        }
    }
}

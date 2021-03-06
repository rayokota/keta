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

import io.kcache.keta.version.TxVersionedCache;
import io.kcache.keta.version.VersionedCache;
import io.kcache.utils.Streams;
import org.apache.omid.transaction.RollbackException;
import org.apache.omid.transaction.Transaction;
import org.apache.omid.transaction.TransactionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


public class TransactionConflictTest {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionConflictTest.class);

    private static final String TEST_TABLE = "test-table";
    private static final String TEST_TABLE2 = "test-table2";

    private byte[] rowId1 = "row1".getBytes();
    private byte[] rowId2 = "row2".getBytes();

    private byte[] dataValue1 = "testWrite-1".getBytes();
    private byte[] dataValue2 = "testWrite-2".getBytes();

    private TransactionManager tm;
    private TxVersionedCache versionedCache;
    private TxVersionedCache versionedCache2;

    @BeforeEach
    public void setUp() throws Exception {
        tm = KetaTransactionManager.newInstance();
        versionedCache = new TxVersionedCache(new VersionedCache(TEST_TABLE));
        versionedCache2 = new TxVersionedCache(new VersionedCache(TEST_TABLE2));
    }

    @AfterEach
    public void tearDown() throws Exception {
        tm.close();
    }

    @Test
    public void runTestWriteWriteConflict() throws Exception {
        Transaction t1 = tm.begin();
        LOG.info("Transaction created " + t1);

        Transaction t2 = tm.begin();
        LOG.info("Transaction created " + t2);

        KetaTransaction.setCurrentTransaction((KetaTransaction) t1);
        versionedCache.put(rowId1, dataValue1);

        KetaTransaction.setCurrentTransaction((KetaTransaction) t2);
        versionedCache.put(rowId1, dataValue2);

        tm.commit(t2);

        try {
            tm.commit(t1);
            fail("Transaction should not commit successfully");
        } catch (RollbackException e) {
        }
    }

    @Test
    public void runTestMultiTableConflict() throws Exception {
        Transaction t1 = tm.begin();
        LOG.info("Transaction created " + t1);

        Transaction t2 = tm.begin();
        LOG.info("Transaction created " + t2);

        KetaTransaction.setCurrentTransaction((KetaTransaction) t1);
        versionedCache.put(rowId1, dataValue1);
        versionedCache2.put(rowId1, dataValue1);

        KetaTransaction.setCurrentTransaction((KetaTransaction) t2);
        versionedCache.put(rowId1, dataValue2);
        versionedCache2.put(rowId1, dataValue2);

        tm.commit(t2);

        boolean aborted = false;
        try {
            tm.commit(t1);
            fail("Transaction committed successfully");
        } catch (RollbackException e) {
            aborted = true;
        }
        assertTrue(aborted, "Transaction didn't raise exception");

        Transaction t3 = tm.begin();
        KetaTransaction.setCurrentTransaction((KetaTransaction) t3);
        assertArrayEquals(dataValue2, versionedCache2.get(rowId1).getValue().toByteArray());
    }

    @Test
    public void runTestCleanupAfterConflict() throws Exception {
        Transaction t1 = tm.begin();
        LOG.info("Transaction created " + t1);

        Transaction t2 = tm.begin();
        LOG.info("Transaction created " + t2);

        KetaTransaction.setCurrentTransaction((KetaTransaction) t1);
        versionedCache.put(rowId1, dataValue1);

        KetaTransaction.setCurrentTransaction((KetaTransaction) t2);
        versionedCache.put(rowId1, dataValue2);

        tm.commit(t1);

        boolean aborted = false;
        try {
            tm.commit(t2);
            fail("Transaction committed successfully");
        } catch (RollbackException e) {
            aborted = true;
        }
        assertTrue(aborted, "Transaction didn't raise exception");

        Transaction t3 = tm.begin();
        KetaTransaction.setCurrentTransaction((KetaTransaction) t3);
        assertArrayEquals(dataValue1, versionedCache.get(rowId1).getValue().toByteArray());
    }

    @Test
    public void testCleanupWithDeleteRow() throws Exception {
        int rowcount = 10;
        int count = 0;

        Transaction t1 = tm.begin();
        LOG.info("Transaction created " + t1);

        KetaTransaction.setCurrentTransaction((KetaTransaction) t1);
        for (int i = 0; i < rowcount; i++) {
            versionedCache.put(("test-del" + i).getBytes(), dataValue1);
        }
        tm.commit(t1);

        Transaction t2 = tm.begin();
        LOG.info("Transaction created " + t2);

        KetaTransaction.setCurrentTransaction((KetaTransaction) t2);
        versionedCache.remove(("test-del" + 3).getBytes());

        count = countRows(versionedCache);
        assertEquals(count, rowcount - 1, "Wrong count");

        Transaction t3 = tm.begin();
        LOG.info("Transaction created " + t3);

        KetaTransaction.setCurrentTransaction((KetaTransaction) t3);
        versionedCache.replace(("test-del" + 3).getBytes(), dataValue1, dataValue2);

        tm.commit(t3);

        boolean aborted = false;
        try {
            tm.commit(t2);
            fail("Didn't abort");
        } catch (RollbackException e) {
            aborted = true;
        }
        assertTrue(aborted, "Didn't raise exception");

        Transaction tscan = tm.begin();

        KetaTransaction.setCurrentTransaction((KetaTransaction) tscan);
        count = countRows(versionedCache);
        assertEquals(count, rowcount, "Wrong count");
    }

    public int countRows(TxVersionedCache cache) {
        return (int) Streams.streamOf(cache.all()).count();
    }

    @Test
    public void testBatchedCleanup() throws Exception {
        int rowcount = 10;
        int count = 0;

        Transaction t1 = tm.begin();
        LOG.info("Transaction created " + t1);

        Transaction t2 = tm.begin();
        LOG.info("Transaction created " + t2);

        KetaTransaction.setCurrentTransaction((KetaTransaction) t1);
        versionedCache.put(rowId1, dataValue1);

        KetaTransaction.setCurrentTransaction((KetaTransaction) t2);
        versionedCache.put(rowId1, dataValue2);

        //Add more rows to hit batch
        for (int i = 0; i < rowcount; i++) {
            versionedCache.put(("test-del" + i).getBytes(), dataValue2);
            versionedCache2.put(("test-del" + i).getBytes(), dataValue2);
        }

        // validate rows are really written
        assertEquals(countRows(versionedCache), rowcount + 1, "Unexpected size for read.");
        assertEquals(countRows(versionedCache2), rowcount, "Unexpected size for read.");

        tm.commit(t1);

        boolean aborted = false;
        try {
            tm.commit(t2);
            fail("Transaction commited successfully");
        } catch (RollbackException e) {
            aborted = true;
        }
        assertTrue(aborted, "Transaction didn't raise exception");

        Transaction tscan = tm.begin();

        // validate rows are cleaned
        KetaTransaction.setCurrentTransaction((KetaTransaction) tscan);
        assertEquals(countRows(versionedCache), 1, "Unexpected size for read.");
        assertEquals(countRows(versionedCache2), 0, "Unexpected size for read.");
    }
}

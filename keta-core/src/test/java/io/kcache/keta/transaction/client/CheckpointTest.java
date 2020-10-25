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
import io.kcache.keta.version.VersionedValue;
import org.apache.omid.committable.CommitTable;
import org.apache.omid.transaction.AbstractTransaction.VisibilityLevel;
import org.apache.omid.transaction.Transaction;
import org.apache.omid.transaction.TransactionException;
import org.apache.omid.transaction.TransactionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CheckpointTest {

    private static final Logger LOG = LoggerFactory.getLogger(CheckpointTest.class);

    private static final String TEST_TABLE = "test-table";

    private byte[] rowId1 = "row1".getBytes();

    private byte[] dataValue0 = "testWrite-0".getBytes();
    private byte[] dataValue1 = "testWrite-1".getBytes();
    private byte[] dataValue2 = "testWrite-2".getBytes();
    private byte[] dataValue3 = "testWrite-3".getBytes();

    private TransactionManager tm;
    private TxVersionedCache versionedCache;

    @BeforeEach
    public void setUp() throws Exception {
        tm = KetaTransactionManager.newInstance();
        versionedCache = new TxVersionedCache(new VersionedCache(TEST_TABLE));
    }

    @AfterEach
    public void tearDown() throws Exception {
        tm.close();
    }

    @Test
    public void testFewCheckPoints() throws Exception {

        Transaction tx1 = tm.begin();
        KetaTransaction kdbTx1 = (KetaTransaction) tx1;

        KetaTransaction.setCurrentTransaction((KetaTransaction) tx1);
        versionedCache.put(rowId1, dataValue1);

        VersionedValue v1 = versionedCache.get(rowId1);
        assertArrayEquals(dataValue1, v1.getValue());

        kdbTx1.checkpoint();

        versionedCache.replace(rowId1, dataValue1, dataValue2);

        v1 = versionedCache.get(rowId1);
        assertArrayEquals(dataValue1, v1.getValue());

        kdbTx1.setVisibilityLevel(VisibilityLevel.SNAPSHOT);

        v1 = versionedCache.get(rowId1);
        assertArrayEquals(dataValue2, v1.getValue());

        kdbTx1.checkpoint();

        versionedCache.replace(rowId1, dataValue2, dataValue3);

        v1 = versionedCache.get(rowId1);
        assertArrayEquals(dataValue2, v1.getValue());

        kdbTx1.checkpoint();

        v1 = versionedCache.get(rowId1);
        assertArrayEquals(dataValue3, v1.getValue());

        kdbTx1.setVisibilityLevel(VisibilityLevel.SNAPSHOT_ALL);

        List<VersionedValue> values = versionedCache.getVersions(rowId1);
        assertEquals(3, values.size(), "Expected 3 results and found " + values.size());

        assertArrayEquals(dataValue3, values.get(0).getValue());
        assertArrayEquals(dataValue2, values.get(1).getValue());
        assertArrayEquals(dataValue1, values.get(2).getValue());
    }

    @Test
    public void testSnapshot() throws Exception {
        Transaction tx1 = tm.begin();

        KetaTransaction.setCurrentTransaction((KetaTransaction) tx1);
        versionedCache.put(rowId1, dataValue0);

        tm.commit(tx1);

        tx1 = tm.begin();
        KetaTransaction kdbTx1 = (KetaTransaction) tx1;

        KetaTransaction.setCurrentTransaction((KetaTransaction) tx1);
        VersionedValue v1 = versionedCache.get(rowId1);
        assertArrayEquals(dataValue0, v1.getValue());

        versionedCache.replace(rowId1, dataValue0, dataValue1);

        v1 = versionedCache.get(rowId1);
        assertArrayEquals(dataValue1, v1.getValue());

        kdbTx1.checkpoint();

        versionedCache.replace(rowId1, dataValue1, dataValue2);

        v1 = versionedCache.get(rowId1);
        assertArrayEquals(dataValue1, v1.getValue());

        kdbTx1.setVisibilityLevel(VisibilityLevel.SNAPSHOT);

        v1 = versionedCache.get(rowId1);
        assertArrayEquals(dataValue2, v1.getValue());
    }

    @Test
    public void testSnapshotAll() throws Exception {
        Transaction tx1 = tm.begin();

        KetaTransaction.setCurrentTransaction((KetaTransaction) tx1);
        versionedCache.put(rowId1, dataValue0);

        tm.commit(tx1);

        tx1 = tm.begin();
        KetaTransaction kdbTx1 = (KetaTransaction) tx1;

        KetaTransaction.setCurrentTransaction((KetaTransaction) tx1);
        VersionedValue v1 = versionedCache.get(rowId1);
        assertArrayEquals(dataValue0, v1.getValue());

        versionedCache.replace(rowId1, dataValue0, dataValue1);

        v1 = versionedCache.get(rowId1);
        assertArrayEquals(dataValue1, v1.getValue());

        kdbTx1.checkpoint();

        versionedCache.replace(rowId1, dataValue1, dataValue2);

        v1 = versionedCache.get(rowId1);
        assertArrayEquals(dataValue1, v1.getValue());

        kdbTx1.setVisibilityLevel(VisibilityLevel.SNAPSHOT_ALL);

        List<VersionedValue> values = versionedCache.getVersions(rowId1);
        assertEquals(3, values.size(), "Expected 3 results and found " + values.size());

        assertArrayEquals(dataValue2, values.get(0).getValue());
        assertArrayEquals(dataValue1, values.get(1).getValue());
        assertArrayEquals(dataValue0, values.get(2).getValue());
    }

    @Test
    public void testSnapshotExcludeCurrent() throws Exception {
        Transaction tx1 = tm.begin();
        KetaTransaction kdbTx1 = (KetaTransaction) tx1;

        KetaTransaction.setCurrentTransaction((KetaTransaction) tx1);
        versionedCache.put(rowId1, dataValue1);

        VersionedValue v1 = versionedCache.get(rowId1);
        assertArrayEquals(dataValue1, v1.getValue());

        kdbTx1.checkpoint();

        versionedCache.replace(rowId1, dataValue1, dataValue2);

        v1 = versionedCache.get(rowId1);
        assertArrayEquals(dataValue1, v1.getValue());

        kdbTx1.setVisibilityLevel(VisibilityLevel.SNAPSHOT_EXCLUDE_CURRENT);

        v1 = versionedCache.get(rowId1);
        assertArrayEquals(dataValue1, v1.getValue());
    }

    @Test
    public void testDeleteAfterCheckpoint() throws Exception {
        Transaction tx1 = tm.begin();

        KetaTransaction.setCurrentTransaction((KetaTransaction) tx1);
        versionedCache.put(rowId1, dataValue1);

        tm.commit(tx1);

        Transaction tx2 = tm.begin();

        KetaTransaction kdbTx2 = (KetaTransaction) tx2;

        kdbTx2.checkpoint();

        versionedCache.remove(rowId1);

        try {
            tm.commit(tx2);
        } catch (TransactionException e) {
            Assertions.fail();
        }
    }

    @Test
    @Disabled
    public void testOutOfCheckpoints() throws Exception {
        Transaction tx1 = tm.begin();
        KetaTransaction kdbTx1 = (KetaTransaction) tx1;

        for (int i = 0; i < CommitTable.MAX_CHECKPOINTS_PER_TXN - 1; ++i) {
            kdbTx1.checkpoint();
        }

        try {
            kdbTx1.checkpoint();
            Assertions.fail();
        } catch (TransactionException e) {
            // expected
        }
    }
}

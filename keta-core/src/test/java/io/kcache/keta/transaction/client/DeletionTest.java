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
import io.kcache.KeyValue;
import io.kcache.utils.Streams;
import org.apache.omid.transaction.Transaction;
import org.apache.omid.transaction.TransactionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class DeletionTest {

    private static final Logger LOG = LoggerFactory.getLogger(DeletionTest.class);

    private static final String TEST_TABLE = "test-table";

    private byte[] dataValue1 = "testWrite-1".getBytes();

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
    public void runTestDeleteRow() throws Exception {
        Transaction t1 = tm.begin();
        LOG.info("Transaction created " + t1);

        int rowsWritten = 10;

        KetaTransaction.setCurrentTransaction((KetaTransaction) t1);
        writeRows(versionedCache, rowsWritten);

        tm.commit(t1);

        Transaction t2 = tm.begin();

        KetaTransaction.setCurrentTransaction((KetaTransaction) t2);
        versionedCache.remove(("test-del" + 0).getBytes());

        Transaction tscan = tm.begin();

        KetaTransaction.setCurrentTransaction((KetaTransaction) tscan);
        Iterator<KeyValue<byte[], VersionedValue>> iter = versionedCache.range(
            ("test-del" + 0).getBytes(), true, ("test-del" + 9).getBytes(), true);
        int rowsRead = (int) Streams.streamOf(iter).count();
        assertEquals(rowsRead, rowsWritten, "Expected " + rowsWritten + " rows but " + rowsRead + " found");

        tm.commit(t2);

        tscan = tm.begin();

        KetaTransaction.setCurrentTransaction((KetaTransaction) tscan);
        iter = versionedCache.range(
            ("test-del" + 0).getBytes(), true, ("test-del" + 9).getBytes(), true);
        rowsRead = (int) Streams.streamOf(iter).count();
        assertEquals(rowsRead, rowsWritten - 1, "Expected " + (rowsWritten - 1) + " rows but " + rowsRead + " found");
    }

    private void writeRows(TxVersionedCache versionCache, int rowcount) {
        for (int i = 0; i < rowcount; i++) {
            byte[] row = ("test-del" + i).getBytes();
            versionCache.put(row, dataValue1);
        }
    }
}

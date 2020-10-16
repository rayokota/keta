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
import org.apache.omid.transaction.Transaction;
import org.apache.omid.transaction.TransactionManager;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class MultiplePutTest {

    private static final Logger LOG = LoggerFactory.getLogger(MultiplePutTest.class);

    private static final String TEST_TABLE = "test-table";

    @Test
    public void testManyManyPutsInDifferentRowsAreInTheTableAfterCommit() throws Exception {
        final int NUM_ROWS_TO_ADD = 50;

        TransactionManager tm = KetsieTransactionManager.newInstance();
        TxVersionedCache versionedCache = new TxVersionedCache(new VersionedCache(TEST_TABLE));

        Transaction tx = tm.begin();

        KetsieTransaction.setCurrentTransaction((KetsieTransaction) tx);
        for (int i = 0; i <= NUM_ROWS_TO_ADD; i++) {
            versionedCache.put(new byte[]{(byte)i}, ("testData" + i).getBytes());
        }

        tm.commit(tx);

        tx = tm.begin();
        KetsieTransaction.setCurrentTransaction((KetsieTransaction) tx);
        assertArrayEquals(("testData" + 0).getBytes(), versionedCache.get(new byte[]{0}).getValue());
        assertArrayEquals(("testData" + NUM_ROWS_TO_ADD).getBytes(), versionedCache.get(new byte[]{NUM_ROWS_TO_ADD}).getValue());
    }

    @Test
    public void testGetFromNonExistentRowAfterMultiplePutsReturnsNoResult() throws Exception {
        final int NUM_ROWS_TO_ADD = 10;

        TransactionManager tm = KetsieTransactionManager.newInstance();
        TxVersionedCache versionedCache = new TxVersionedCache(new VersionedCache(TEST_TABLE));

        Transaction tx = tm.begin();

        KetsieTransaction.setCurrentTransaction((KetsieTransaction) tx);
        for (int i = 0; i <= NUM_ROWS_TO_ADD; i++) {
            versionedCache.put(new byte[]{(byte)i}, ("testData" + i).getBytes());
        }

        tm.commit(tx);

        tx = tm.begin();
        KetsieTransaction.setCurrentTransaction((KetsieTransaction) tx);
        assertNull(versionedCache.get(new byte[]{NUM_ROWS_TO_ADD + 5}));
    }
}

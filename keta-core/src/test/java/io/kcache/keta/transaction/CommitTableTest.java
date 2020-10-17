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
package io.kcache.keta.transaction;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.ListenableFuture;
import io.kcache.Cache;
import io.kcache.utils.InMemoryCache;
import org.apache.omid.committable.CommitTable;
import org.apache.omid.committable.CommitTable.Client;
import org.apache.omid.committable.CommitTable.CommitTimestamp;
import org.apache.omid.committable.CommitTable.Writer;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommitTableTest {

    private static final Logger LOG = LoggerFactory.getLogger(io.kcache.keta.transaction.CommitTableTest.class);

    @Test
    public void testBasicBehaviour() throws Throwable {
        Cache<Long, Long> cache = new InMemoryCache<>();
        KetaCommitTable commitTable = new KetaCommitTable(cache);

        Writer writer = commitTable.getWriter();
        Client client = commitTable.getClient();

        // Test that the first time the table is empty
        assertEquals(cache.size(), 0, "Rows should be 0!");

        // Test the successful creation of 1000 txs in the table
        for (int i = 0; i < 1000; i += CommitTable.MAX_CHECKPOINTS_PER_TXN) {
            writer.addCommittedTransaction(i, i + 1);
        }
        writer.flush();
        assertEquals(cache.size(), 1000 / CommitTable.MAX_CHECKPOINTS_PER_TXN, "Rows should be 1000!");

        // Test the we get the right commit timestamps for each previously inserted tx
        for (long i = 0; i < 1000; i++) {
            Optional<CommitTimestamp> commitTimestamp = client.getCommitTimestamp(i).get();
            assertTrue(commitTimestamp.isPresent());
            assertTrue(commitTimestamp.get().isValid());
            long ct = commitTimestamp.get().getValue();
            long expected = i - (i % CommitTable.MAX_CHECKPOINTS_PER_TXN) + 1;
            assertEquals(ct, expected, "Commit timestamp should be " + expected);
        }
        assertEquals(cache.size(), 1000 / CommitTable.MAX_CHECKPOINTS_PER_TXN, "Rows should be 1000!");

        // Test the successful deletion of the 1000 txs
        Future<Void> f;
        for (long i = 0; i < 1000; i += CommitTable.MAX_CHECKPOINTS_PER_TXN) {
            f = client.deleteCommitEntry(i);
            f.get();
        }
        assertEquals(cache.size(), 0, "Rows should be 0!");

        // Test we don't get a commit timestamp for a non-existent transaction id in the table
        Optional<CommitTimestamp> commitTimestamp = client.getCommitTimestamp(0).get();
        assertFalse(commitTimestamp.isPresent(), "Commit timestamp should not be present");

        // Test that the first time, the low watermark family in table is empty
        assertEquals(cache.size(), 0, "Rows should be 0!");

        // Test the unsuccessful read of the low watermark the first time
        ListenableFuture<Long> lowWatermarkFuture = client.readLowWatermark();
        assertEquals(lowWatermarkFuture.get(), Long.valueOf(0), "Low watermark should be 0");

        // Test the successful update of the low watermark
        for (int lowWatermark = 0; lowWatermark < 1000; lowWatermark++) {
            writer.updateLowWatermark(lowWatermark);
        }
        writer.flush();
        assertEquals(cache.size(), 1, "Should there be only row!");

        // Test the successful read of the low watermark
        lowWatermarkFuture = client.readLowWatermark();
        long lowWatermark = lowWatermarkFuture.get();
        assertEquals(lowWatermark, 999, "Low watermark should be 999");
        assertEquals(cache.size(), 1, "Should there be only one row");
    }

    @Test
    public void testCheckpoints() throws Throwable {
        Cache<Long, Long> cache = new InMemoryCache<>();
        KetaCommitTable commitTable = new KetaCommitTable(cache);

        Writer writer = commitTable.getWriter();
        Client client = commitTable.getClient();

        // Test that the first time the table is empty
        assertEquals(cache.size(), 0, "Rows should be 0!");

        long st = 0;
        long ct = 1;

        // Add a single commit that may represent many checkpoints
        writer.addCommittedTransaction(st, ct);
        writer.flush();

        for (int i = 0; i < CommitTable.MAX_CHECKPOINTS_PER_TXN; ++i) {
            Optional<CommitTimestamp> commitTimestamp = client.getCommitTimestamp(i).get();
            assertTrue(commitTimestamp.isPresent());
            assertTrue(commitTimestamp.get().isValid());
            assertEquals(ct, commitTimestamp.get().getValue());
        }

        // try invalidate based on start timestamp from a checkpoint
        assertFalse(client.tryInvalidateTransaction(st + 1).get());

        long st2 = 100;
        long ct2 = 101;

        // now invalidate a not committed transaction and then commit
        assertTrue(client.tryInvalidateTransaction(st2 + 1).get());
        assertFalse(writer.atomicAddCommittedTransaction(st2, ct2));

        //test delete
        client.deleteCommitEntry(st2 + 1).get();
        //now committing should work
        assertTrue(writer.atomicAddCommittedTransaction(st2, ct2));
    }

    @Test
    public void testTransactionInvalidation() throws Throwable {
        // Prepare test
        final int TX1_ST = 0;
        final int TX1_CT = TX1_ST + 1;
        final int TX2_ST = CommitTable.MAX_CHECKPOINTS_PER_TXN;
        final int TX2_CT = TX2_ST + 1;

        Cache<Long, Long> cache = new InMemoryCache<>();
        KetaCommitTable commitTable = new KetaCommitTable(cache);

        // Components under test
        Writer writer = commitTable.getWriter();
        Client client = commitTable.getClient();

        // Test that initially the table is empty
        assertEquals(cache.size(), 0, "Rows should be 0!");

        // Test that a transaction can be added properly to the commit table
        writer.addCommittedTransaction(TX1_ST, TX1_CT);
        writer.flush();
        Optional<CommitTimestamp> commitTimestamp = client.getCommitTimestamp(TX1_ST).get();
        assertTrue(commitTimestamp.isPresent());
        assertTrue(commitTimestamp.get().isValid());
        long ct = commitTimestamp.get().getValue();
        assertEquals(ct, TX1_CT, "Commit timestamp should be " + TX1_CT);

        // Test that a committed transaction cannot be invalidated and
        // preserves its commit timestamp after that
        boolean wasInvalidated = client.tryInvalidateTransaction(TX1_ST).get();
        assertFalse(wasInvalidated, "Transaction should not be invalidated");

        commitTimestamp = client.getCommitTimestamp(TX1_ST).get();
        assertTrue(commitTimestamp.isPresent());
        assertTrue(commitTimestamp.get().isValid());
        ct = commitTimestamp.get().getValue();
        assertEquals(ct, TX1_CT, "Commit timestamp should be " + TX1_CT);

        // Test that a non-committed transaction can be invalidated...
        wasInvalidated = client.tryInvalidateTransaction(TX2_ST).get();
        assertTrue(wasInvalidated, "Transaction should be invalidated");
        commitTimestamp = client.getCommitTimestamp(TX2_ST).get();
        assertTrue(commitTimestamp.isPresent());
        assertFalse(commitTimestamp.get().isValid());
        ct = commitTimestamp.get().getValue();
        assertEquals(ct, CommitTable.INVALID_TRANSACTION_MARKER,
            "Commit timestamp should be " + CommitTable.INVALID_TRANSACTION_MARKER);
        // ...and that if it has been already invalidated, it remains
        // invalidated when someone tries to commit it
        writer.addCommittedTransaction(TX2_ST, TX2_CT);
        writer.flush();
        commitTimestamp = client.getCommitTimestamp(TX2_ST).get();
        assertTrue(commitTimestamp.isPresent());
        assertFalse(commitTimestamp.get().isValid());
        ct = commitTimestamp.get().getValue();
        assertEquals(ct, CommitTable.INVALID_TRANSACTION_MARKER,
            "Commit timestamp should be " + CommitTable.INVALID_TRANSACTION_MARKER);

        // Test that at the end of the test, the commit table contains 2
        // elements, which correspond to the two rows added in the test
        assertEquals(cache.size(), 2, "Rows should be 2!");
    }
}

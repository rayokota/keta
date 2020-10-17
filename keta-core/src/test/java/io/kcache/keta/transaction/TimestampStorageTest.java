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

import io.kcache.utils.InMemoryCache;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TimestampStorageTest {

    private static final Logger LOG = LoggerFactory.getLogger(io.kcache.keta.transaction.TimestampStorageTest.class);

    @Test
    public void testTimestampStorage() throws Exception {

        final long INITIAL_TS_VALUE = 0;
        KetaTimestampStorage tsStorage = new KetaTimestampStorage(new InMemoryCache<>());

        // Test that the first time we get the timestamp is the initial value
        assertEquals(tsStorage.getMaxTimestamp(), INITIAL_TS_VALUE, "Initial value should be " + INITIAL_TS_VALUE);

        // Test that updating the timestamp succeeds when passing the initial value as the previous one
        long newTimestamp = 1;
        tsStorage.updateMaxTimestamp(INITIAL_TS_VALUE, newTimestamp);

        // Test setting a new timestamp fails (exception is thrown) when passing a wrong previous max timestamp
        long wrongTimestamp = 20;
        try {
            tsStorage.updateMaxTimestamp(wrongTimestamp, newTimestamp);
            Assertions.fail("Shouldn't update");
        } catch (IOException e) {
            // Correct behavior
        }
        assertEquals(tsStorage.getMaxTimestamp(), newTimestamp, "Value should be still " + newTimestamp);

        // Test we can set a new timestamp when passing the right previous max timestamp
        long veryNewTimestamp = 40;
        tsStorage.updateMaxTimestamp(newTimestamp, veryNewTimestamp);
        assertEquals(tsStorage.getMaxTimestamp(), veryNewTimestamp, "Value should be " + veryNewTimestamp);
    }
}

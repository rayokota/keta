/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.kcache.ketsie.kafka.serialization;

import io.kcache.ketsie.version.VersionedValue;
import io.kcache.ketsie.version.VersionedValues;
import org.junit.Test;

import java.util.NavigableMap;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;

public class KafkaValueSerializerTest {

    @Test
    public void testSerializer() throws Exception {
        KafkaValueSerde serde = new KafkaValueSerde();
        NavigableMap<Long, VersionedValue> map = new TreeMap<>();
        map.put(2L, new VersionedValue(2, "hi".getBytes()));
        map.put(4L, new VersionedValue(4, "bye".getBytes()));
        VersionedValues values = new VersionedValues(1, map);
        byte[] bytes = serde.serializer().serialize(null, values);
        VersionedValues values2 = serde.deserializer().deserialize(null, bytes);
        assertEquals(values, values2);
    }
}

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
package io.kcache.keta.kafka.serialization;

import com.google.protobuf.ByteString;
import io.kcache.keta.pb.VersionedValue;
import io.kcache.keta.version.VersionedValues;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.NavigableMap;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class KafkaValueSerializerTest {

    @Test
    public void testSerializer() throws Exception {
        KafkaValueSerde serde = new KafkaValueSerde();
        NavigableMap<Long, VersionedValue> map = new TreeMap<>();
        map.put(2L, VersionedValue.newBuilder()
            .setVersion(2)
            .setCreate(1)
            .setSequence(0)
            .setValue(ByteString.copyFrom("hi", StandardCharsets.UTF_8))
            .build());
        map.put(4L, VersionedValue.newBuilder()
            .setVersion(4)
            .setCreate(2)
            .setSequence(1)
            .setValue(ByteString.copyFrom("bye", StandardCharsets.UTF_8))
            .build());
        VersionedValues values = new VersionedValues(1, map);
        byte[] bytes = serde.serializer().serialize(null, values);
        VersionedValues values2 = serde.deserializer().deserialize(null, bytes);
        assertEquals(values, values2);
    }
}

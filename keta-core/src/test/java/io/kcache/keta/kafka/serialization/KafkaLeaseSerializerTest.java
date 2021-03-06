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

import io.kcache.keta.pb.Lease;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class KafkaLeaseSerializerTest {

    @Test
    public void testSerializer() throws Exception {
        KafkaProtobufSerde<Lease> serde = new KafkaProtobufSerde<>(Lease.class);
        Lease lease = Lease.newBuilder()
            .setID(1)
            .setTTL(1000)
            .setExpiry(System.currentTimeMillis())
            .build();
        byte[] bytes = serde.serializer().serialize(null, lease);
        Lease lease2 = serde.deserializer().deserialize(null, bytes);
        assertEquals(lease, lease2);
    }
}

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

import io.kcache.ketsie.lease.Lease;
import io.kcache.ketsie.version.VersionedValue;
import io.kcache.ketsie.version.VersionedValues;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericArray;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import static io.kcache.ketsie.kafka.serialization.KafkaValueSerde.GENERIC;
import static io.kcache.ketsie.kafka.serialization.KafkaValueSerde.MAGIC_BYTE;

public class KafkaLeaseDeserializer implements Deserializer<Lease> {
    private final static Logger LOG = LoggerFactory.getLogger(KafkaLeaseDeserializer.class);

    public KafkaLeaseDeserializer() {
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public Lease deserialize(String topic, byte[] payload) throws SerializationException {
        if (payload == null) {
            return null;
        }
        ByteBuffer buffer = getByteBuffer(payload);
        long id = buffer.getLong();
        long ttl = buffer.getLong();
        long expiry = buffer.getLong();
        return new Lease(id, ttl, expiry);
    }

    private ByteBuffer getByteBuffer(byte[] payload) {
        ByteBuffer buffer = ByteBuffer.wrap(payload);
        if (buffer.get() != MAGIC_BYTE) {
            throw new SerializationException("Unknown magic byte!");
        }
        return buffer;
    }

    @Override
    public void close() {
    }
}

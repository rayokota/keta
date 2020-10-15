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

public class KafkaValueDeserializer implements Deserializer<VersionedValues> {
    private final static Logger LOG = LoggerFactory.getLogger(KafkaValueDeserializer.class);

    private final DecoderFactory decoderFactory = DecoderFactory.get();
    private DatumReader<GenericRecord> reader;

    public KafkaValueDeserializer(Schema avroSchema) {
        this.reader = new GenericDatumReader<>(avroSchema, avroSchema, GENERIC);
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public VersionedValues deserialize(String topic, byte[] payload) throws SerializationException {
        if (payload == null) {
            return null;
        }
        try {
            ByteBuffer buffer = getByteBuffer(payload);
            int length = buffer.limit() - 1;
            int start = buffer.position() + buffer.arrayOffset();
            GenericRecord record = reader.read(
                null, decoderFactory.binaryDecoder(buffer.array(), start, length, null));
            return toValue(record);
        } catch (IOException | RuntimeException e) {
            // avro deserialization may throw AvroRuntimeException, NullPointerException, etc
            LOG.error("Error deserializing Avro value " + e.getMessage());
            throw new SerializationException("Error deserializing Avro value", e);
        }
    }

    private ByteBuffer getByteBuffer(byte[] payload) {
        ByteBuffer buffer = ByteBuffer.wrap(payload);
        if (buffer.get() != MAGIC_BYTE) {
            throw new SerializationException("Unknown magic byte!");
        }
        return buffer;
    }

    private VersionedValues toValue(GenericRecord genericRecord) {
        NavigableMap<Long, VersionedValue> map = new TreeMap<>();
        Integer generationId = (Integer) genericRecord.get(0);
        GenericArray<GenericRecord> array = (GenericArray<GenericRecord>) genericRecord.get(1);
        for (GenericRecord record : array) {
            Long version = (Long) record.get(0);
            Long commit = (Long) record.get(1);
            boolean deleted = (Boolean) record.get(2);
            Long lease = (Long) record.get(3);
            ByteBuffer bytes = (ByteBuffer) record.get(4);
            map.put(version, new VersionedValue(version, commit, deleted, bytes.array(), lease));
        }
        return new VersionedValues(generationId, map);
    }

    @Override
    public void close() {
    }
}

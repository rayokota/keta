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
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

import static io.kcache.ketsie.kafka.serialization.KafkaValueSerde.GENERIC;
import static io.kcache.ketsie.kafka.serialization.KafkaValueSerde.MAGIC_BYTE;

public class KafkaValueSerializer implements Serializer<VersionedValues> {
    private final static Logger LOG = LoggerFactory.getLogger(KafkaValueSerializer.class);

    private final EncoderFactory encoderFactory = EncoderFactory.get();
    private Schema avroSchema;
    private DatumWriter<Object> writer;

    public KafkaValueSerializer(Schema avroSchema) {
        this.avroSchema = avroSchema;
        this.writer = new GenericDatumWriter<>(avroSchema, GENERIC);
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public byte[] serialize(String topic, VersionedValues object) {
        if (object == null) {
            return null;
        }
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write(MAGIC_BYTE);
            BinaryEncoder encoder = encoderFactory.directBinaryEncoder(out, null);
            writer.write(toArray(object), encoder);
            encoder.flush();
            byte[] bytes = out.toByteArray();
            out.close();
            return bytes;
        } catch (IOException | RuntimeException e) {
            // avro serialization can throw AvroRuntimeException, NullPointerException,
            // ClassCastException, etc
            LOG.error("Error serializing Avro value " + e.getMessage());
            throw new SerializationException("Error serializing Avro value", e);
        }
    }

    private GenericRecord toArray(VersionedValues object) {
        GenericRecordBuilder builder = new GenericRecordBuilder(avroSchema);
        builder.set("_generation_id", object.getGenerationId());
        Schema arraySchema = avroSchema.getField("_values").schema();
        List<GenericRecord> records = new GenericData.Array<>(object.getValues().size(), arraySchema);
        for (VersionedValue versionedValue : object.getValues().values()) {
            GenericRecordBuilder nested = new GenericRecordBuilder(arraySchema.getElementType());
            nested.set("_version", versionedValue.getVersion());
            nested.set("_commit", versionedValue.getCommit());
            nested.set("_deleted", versionedValue.isDeleted());
            nested.set("_value", ByteBuffer.wrap(versionedValue.getValue()));
            records.add(nested.build());
        }
        builder.set("_values", records);
        return builder.build();
    }

    @Override
    public void close() {
    }
}

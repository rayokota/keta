/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import io.kcache.ketsie.version.VersionedValues;
import org.apache.avro.Conversions;
import org.apache.avro.JsonProperties;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;
import java.util.NavigableMap;

public class KafkaValueSerde implements Serde<VersionedValues> {
    protected static final byte MAGIC_BYTE = 0x0;

    final static GenericData GENERIC = new GenericData();

    private final Serde<VersionedValues> inner;

    public KafkaValueSerde() {
        Schema avroSchema = getValueSchema();
        inner = Serdes.serdeFrom(new KafkaValueSerializer(avroSchema), new KafkaValueDeserializer(avroSchema));
    }

    @Override
    public Serializer<VersionedValues> serializer() {
        return inner.serializer();
    }

    @Override
    public Deserializer<VersionedValues> deserializer() {
        return inner.deserializer();
    }

    @Override
    public void configure(final Map<String, ?> serdeConfig, final boolean isSerdeForRecordKeys) {
        inner.serializer().configure(serdeConfig, isSerdeForRecordKeys);
        inner.deserializer().configure(serdeConfig, isSerdeForRecordKeys);
    }

    @Override
    public void close() {
        inner.serializer().close();
        inner.deserializer().close();
    }

    private static org.apache.avro.Schema getValueSchema() {
        SchemaBuilder.FieldAssembler<org.apache.avro.Schema> valueSchemaBuilder =
            SchemaBuilder.record("versioned_value").fields();
        valueSchemaBuilder = valueSchemaBuilder
            .name("_version").type().longType().noDefault()
            .name("_commit").type().longType().noDefault()
            .name("_deleted").type().booleanType().noDefault()
            .name("_value").type().bytesType().noDefault();
        org.apache.avro.Schema valueSchema = SchemaBuilder.array().items(valueSchemaBuilder.endRecord());
        SchemaBuilder.FieldAssembler<org.apache.avro.Schema> valuesSchemaBuilder =
            SchemaBuilder.record("versioned_values").fields();
        valuesSchemaBuilder
            .name("_generation_id").type().intType().noDefault()
            .name("_values").type(valueSchema).noDefault();
        return valuesSchemaBuilder.endRecord();
    }
}
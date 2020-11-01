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
import io.kcache.keta.pb.VersionedValueList;
import io.kcache.keta.version.VersionedValues;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

import static io.kcache.keta.kafka.serialization.KafkaValueSerde.MAGIC_BYTE;

public class KafkaValueDeserializer implements Deserializer<VersionedValues> {
    private final static Logger LOG = LoggerFactory.getLogger(KafkaValueDeserializer.class);

    public KafkaValueDeserializer() {
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
            readMagicByte(payload);
            VersionedValueList list = VersionedValueList.parseFrom(ByteString.copyFrom(payload, 1, payload.length - 1));
            return new VersionedValues(list);
        } catch (IOException | RuntimeException e) {
            LOG.error("Error deserializing value " + e.getMessage());
            throw new SerializationException("Error deserializing value", e);
        }
    }

    private void readMagicByte(byte[] payload) {
        if (payload.length == 0 || payload[0] != MAGIC_BYTE) {
            throw new SerializationException("Unknown magic byte!");
        }
    }

    @Override
    public void close() {
    }
}

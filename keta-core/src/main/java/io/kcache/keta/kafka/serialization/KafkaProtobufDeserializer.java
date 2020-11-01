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
import com.google.protobuf.Message;
import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;

import static io.kcache.keta.kafka.serialization.KafkaValueSerde.MAGIC_BYTE;

public class KafkaProtobufDeserializer<T extends Message> implements Deserializer<T> {
    private final static Logger LOG = LoggerFactory.getLogger(KafkaProtobufDeserializer.class);

    private final Class<T> type;
    private final Method parseMethod;

    public KafkaProtobufDeserializer(Class<T> type) {
        try {
            this.type = type;
            this.parseMethod = type.getDeclaredMethod("parseFrom", ByteString.class);
        } catch (Exception e) {
            throw new ConfigException("Class " + type.getCanonicalName()
                + " is not a valid protobuf message class", e);
        }
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    @SuppressWarnings("unchecked")
    public T deserialize(String topic, byte[] payload) throws SerializationException {
        try {
            if (payload == null) {
                return null;
            }
            readMagicByte(payload);
            return (T) parseMethod.invoke(null, ByteString.copyFrom(payload, 1, payload.length - 1));
        } catch (Exception e) {
            throw new SerializationException(e);
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

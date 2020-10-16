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
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

import static io.kcache.ketsie.kafka.serialization.KafkaValueSerde.MAGIC_BYTE;

public class KafkaLeaseSerializer implements Serializer<Lease> {
    private final static Logger LOG = LoggerFactory.getLogger(KafkaLeaseSerializer.class);

    public KafkaLeaseSerializer() {
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public byte[] serialize(String topic, Lease lease) {
        if (lease == null) {
            return null;
        }
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write(MAGIC_BYTE);
            ByteBuffer buf = ByteBuffer.wrap(new byte[24]);
            buf.putLong(lease.getId());
            buf.putLong(lease.getTtl());
            buf.putLong(lease.getExpiry());
            out.write(buf.array());
            byte[] bytes = out.toByteArray();
            out.close();
            return bytes;
        } catch (IOException | RuntimeException e) {
            LOG.error("Error serializing lease " + e.getMessage());
            throw new SerializationException("Error serializing lease", e);
        }
    }

    @Override
    public void close() {
    }
}

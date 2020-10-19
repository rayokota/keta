/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.kcache.keta.utils;

import com.google.protobuf.ByteString;
import io.etcd.jetcd.api.KeyValue;
import io.kcache.keta.version.VersionedValue;

public class ProtoUtils {
    public static KeyValue toKeyValue(byte[] key, VersionedValue value) {
        KeyValue.Builder builder = KeyValue.newBuilder()
            .setKey(ByteString.copyFrom(key));
        if (value != null) {
            builder.setValue(ByteString.copyFrom(value.getValue()))
                .setVersion(value.getVersion());
        }
        return builder.build();
    }
}
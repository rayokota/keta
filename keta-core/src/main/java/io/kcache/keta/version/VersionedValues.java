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

package io.kcache.keta.version;

import io.kcache.keta.pb.VersionedValue;
import io.kcache.keta.pb.VersionedValueList;

import java.util.NavigableMap;
import java.util.Objects;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;

public class VersionedValues {
    private final int generationId;
    private final NavigableMap<Long, VersionedValue> values;

    public VersionedValues(VersionedValueList valueList) {
        this.generationId = valueList.getGeneration();
        this.values = valueList.getValuesList().stream()
            .collect(Collectors.toMap(
                VersionedValue::getVersion,
                v -> v,
                (v1, v2) -> {
                    throw new RuntimeException(String.format("Duplicate key for values %s and %s", v1, v2));
                },
                ConcurrentSkipListMap::new));
    }

    public VersionedValues(int generationId) {
        this.generationId = generationId;
        this.values = new ConcurrentSkipListMap<>();
    }

    public VersionedValues(int generationId, NavigableMap<Long, VersionedValue> values) {
        this.generationId = generationId;
        this.values = values;
    }

    public int getGenerationId() {
        return generationId;
    }

    public NavigableMap<Long, VersionedValue> getValues() {
        return values;
    }

    public VersionedValueList toList() {
        return VersionedValueList.newBuilder()
            .setGeneration(generationId)
            .addAllValues(values.values())
            .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        VersionedValues that = (VersionedValues) o;
        return generationId == that.generationId
            && Objects.equals(values, that.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(generationId, values);
    }
}


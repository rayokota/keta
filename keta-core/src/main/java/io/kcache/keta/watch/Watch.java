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

package io.kcache.keta.watch;

import com.google.protobuf.ByteString;

import java.util.Objects;

public class Watch {
    private final long id;
    private final ByteString key;
    private final ByteString end;

    public Watch(long id, ByteString key, ByteString end) {
        this.id = id;
        this.key = key;
        this.end = end;
    }

    public long getID() {
        return id;
    }

    public ByteString getKey() {
        return key;
    }

    public ByteString getEnd() {
        return end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Watch watch = (Watch) o;
        return id == watch.id
            && Objects.equals(key, watch.key)
            && Objects.equals(end, watch.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, key, end);
    }
}


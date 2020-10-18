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

package io.kcache.keta.server.leader;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * This class implements the protocol for members in a Kafka group. It includes
 * the format of worker state used when joining the group.
 */
class KetaProtocol {

    public static ByteBuffer serializeMetadata(KetaIdentity identity) {
        return identity.toJsonBytes();
    }

    public static KetaIdentity deserializeMetadata(ByteBuffer buffer) {
        return KetaIdentity.fromJson(buffer);
    }

    public static ByteBuffer serializeAssignment(Assignment assignment) {
        return assignment.toJsonBytes();
    }

    public static Assignment deserializeAssignment(ByteBuffer buffer) {
        return Assignment.fromJson(buffer);
    }

    public static class Assignment {
        public static final int CURRENT_VERSION = 1;

        public static final short NO_ERROR = 0;
        // Some members have the same URL, which would result in them not being able to forward requests
        // properly. Usually indicates both are running in containers and using the same hostname.
        // User should explicitly set host.name in their configuration to a routable hostname.
        public static final short DUPLICATE_URLS = 1;

        private final int version;
        private final short error;
        private final String leader;
        private final KetaIdentity leaderIdentity;

        public Assignment(@JsonProperty("error") short error,
                          @JsonProperty("leader") String leader,
                          @JsonProperty("leader_identity") KetaIdentity leaderIdentity) {
            this.version = CURRENT_VERSION;
            this.error = error;
            this.leader = leader;
            this.leaderIdentity = leaderIdentity;
        }

        public static Assignment fromJson(ByteBuffer json) {
            try {
                byte[] jsonBytes = new byte[json.remaining()];
                json.get(jsonBytes);
                return new ObjectMapper().readValue(jsonBytes, Assignment.class);
            } catch (IOException e) {
                throw new IllegalArgumentException("Error deserializing identity information", e);
            }
        }

        @JsonProperty("version")
        public int version() {
            return version;
        }

        @JsonProperty("error")
        public short error() {
            return error;
        }

        @JsonProperty("leader")
        public String leader() {
            return leader;
        }

        @JsonProperty("leader_identity")
        public KetaIdentity leaderIdentity() {
            return leaderIdentity;
        }

        public boolean failed() {
            return error != NO_ERROR;
        }

        public ByteBuffer toJsonBytes() {
            try {
                return ByteBuffer.wrap(new ObjectMapper().writeValueAsBytes(this));
            } catch (IOException e) {
                throw new IllegalArgumentException("Error serializing identity information", e);
            }
        }

        @Override
        public String toString() {
            return "Assignment{"
                + "version=" + version
                + ", error=" + error
                + ", leader='" + leader + '\''
                + ", leaderIdentity=" + leaderIdentity
                + '}';
        }
    }
}

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

package io.kcache.keta.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.kcache.keta.KetaConfig;
import io.kcache.keta.utils.PemUtils;
import org.apache.kafka.common.config.ConfigException;

import java.io.IOException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/*
 * For testing.  Only works in a single-node cluster.
 */
public class SimpleTokenProvider implements TokenProvider {

    private Map<String, String> tokens = new ConcurrentHashMap<>();

    public SimpleTokenProvider(KetaConfig config) throws ConfigException {
    }

    public String getUser(String token) {
        return tokens.get(token);
    }

    public String assignToken(String user) {
        String uuid = UUID.randomUUID().toString();
        tokens.put(uuid, user);
        return uuid;
    }
}

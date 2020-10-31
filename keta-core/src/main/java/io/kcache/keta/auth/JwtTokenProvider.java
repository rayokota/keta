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

public class JwtTokenProvider implements TokenProvider {

    private final Algorithm algorithm;
    private final int ttl;
    private final JWTVerifier verifier;

    public JwtTokenProvider(KetaConfig config) throws ConfigException {
        try {
            String publicKeyPath = config.getString(KetaConfig.TOKEN_PUBLIC_KEY_PATH_CONFIG);
            String privateKeyPath = config.getString(KetaConfig.TOKEN_PRIVATE_KEY_PATH_CONFIG);
            RSAPublicKey publicKey = (RSAPublicKey) PemUtils.readPublicKeyFromFile(publicKeyPath, "RSA");
            RSAPrivateKey privateKey = (RSAPrivateKey) PemUtils.readPrivateKeyFromFile(privateKeyPath, "RSA");
            this.algorithm = Algorithm.RSA256(publicKey, privateKey);
            this.ttl = config.getInt(KetaConfig.TOKEN_TTL_SECS_CONFIG);
            this.verifier = JWT.require(algorithm).build();
        } catch (IOException e) {
            throw new ConfigException("Could not config token provider", e);
        }
    }

    public String getUser(String token) {
        System.out.println("*** get token " + token);
        DecodedJWT verified = verifier.verify(token);
        return verified.getClaim("username").asString();
    }

    public String assignToken(String user) {
        System.out.println("*** assign user " + user);
        return JWT.create()
            .withClaim("username", user)
            .withExpiresAt(new Date(System.currentTimeMillis() + ttl * 1000))
            .sign(algorithm);
    }
}

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
package io.kcache.keta.server.utils;

import io.grpc.netty.GrpcSslContexts;
import io.kcache.keta.KetaConfig;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.common.config.types.Password;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import static io.kcache.keta.KetaConfig.SSL_CLIENT_AUTHENTICATION_NONE;
import static io.kcache.keta.KetaConfig.SSL_CLIENT_AUTHENTICATION_REQUESTED;
import static io.kcache.keta.KetaConfig.SSL_CLIENT_AUTHENTICATION_REQUIRED;

public class SslFactory {

    private String kmfAlgorithm;
    private String tmfAlgorithm;
    private SecurityStore keystore = null;
    private Password keyPassword;
    private SecurityStore truststore;
    private ClientAuth clientAuth;
    private SslContext sslContext;

    public SslFactory(KetaConfig config) {
        this.kmfAlgorithm = config.getString(KetaConfig.SSL_KEYMANAGER_ALGORITHM_CONFIG);
        this.tmfAlgorithm = config.getString(KetaConfig.SSL_TRUSTMANAGER_ALGORITHM_CONFIG);

        try {
            createKeystore(
                config.getString(KetaConfig.SSL_KEYSTORE_TYPE_CONFIG),
                config.getString(KetaConfig.SSL_KEYSTORE_LOCATION_CONFIG),
                config.getPassword(KetaConfig.SSL_KEYSTORE_PASSWORD_CONFIG),
                config.getPassword(KetaConfig.SSL_KEY_PASSWORD_CONFIG)
            );

            createTruststore(
                config.getString(KetaConfig.SSL_TRUSTSTORE_TYPE_CONFIG),
                config.getString(KetaConfig.SSL_TRUSTSTORE_LOCATION_CONFIG),
                config.getPassword(KetaConfig.SSL_TRUSTSTORE_PASSWORD_CONFIG)
            );

            this.clientAuth = getClientAuth(
                config.getString(KetaConfig.SSL_CLIENT_AUTHENTICATION_CONFIG)
            );

            this.sslContext = createSslContext();
        } catch (Exception e) {
            throw new RuntimeException("Error initializing the ssl context for RestService", e);
        }
    }

    private static boolean isNotBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }

    private SslContext createSslContext() throws GeneralSecurityException, IOException {
        if (keystore == null) {
            return null;
        }

        String kmfAlgorithm =
            isNotBlank(this.kmfAlgorithm) ? this.kmfAlgorithm
                : KeyManagerFactory.getDefaultAlgorithm();
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(kmfAlgorithm);
        KeyStore ks = keystore.load();
        Password keyPassword = this.keyPassword != null ? this.keyPassword : keystore.password;
        kmf.init(ks, keyPassword.value().toCharArray());

        SslContextBuilder sslContextBuilder = SslContextBuilder.forServer(kmf);

        if (truststore != null) {
            String tmfAlgorithm =
                isNotBlank(this.tmfAlgorithm) ? this.tmfAlgorithm
                    : TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            KeyStore ts = truststore.load();
            tmf.init(ts);

            sslContextBuilder.trustManager(tmf);
        }

        sslContextBuilder = sslContextBuilder.clientAuth(clientAuth);
        sslContextBuilder = GrpcSslContexts.configure(sslContextBuilder);
        sslContext = sslContextBuilder.build();
        return sslContext;
    }

    private ClientAuth getClientAuth(final String clientAuth) {
        switch (clientAuth) {
            case SSL_CLIENT_AUTHENTICATION_NONE:
                return ClientAuth.NONE;
            case SSL_CLIENT_AUTHENTICATION_REQUESTED:
                return ClientAuth.OPTIONAL;
            case SSL_CLIENT_AUTHENTICATION_REQUIRED:
                return ClientAuth.REQUIRE;
            default:
                throw new ConfigException("Unknown client auth: " + clientAuth);
        }
    }

    /**
     * Returns a configured SslContext.
     *
     * @return SslContext.
     */
    public SslContext sslContext() {
        return sslContext;
    }

    private void createKeystore(String type, String path, Password password, Password keyPassword) {
        if (path == null && password != null) {
            throw new RuntimeException(
                "SSL key store is not specified, but key store password is specified.");
        } else if (path != null && password == null) {
            throw new RuntimeException(
                "SSL key store is specified, but key store password is not specified.");
        } else if (isNotBlank(path) && isNotBlank(password.value())) {
            this.keystore = new SecurityStore(type, path, password);
            this.keyPassword = keyPassword;
        }
    }

    private void createTruststore(String type, String path, Password password) {
        if (path == null && password != null) {
            throw new RuntimeException(
                "SSL trust store is not specified, but trust store password is specified.");
        } else if (isNotBlank(path)) {
            this.truststore = new SecurityStore(type, path, password);
        }
    }

    private static class SecurityStore {

        private final String type;
        private final String path;
        private final Password password;

        private SecurityStore(String type, String path, Password password) {
            this.type = type == null ? KeyStore.getDefaultType() : type;
            this.path = path;
            this.password = password;
        }

        private KeyStore load() throws GeneralSecurityException, IOException {
            FileInputStream in = null;
            try {
                KeyStore ks = KeyStore.getInstance(type);
                in = new FileInputStream(path);
                char[] passwordChars = password != null ? password.value().toCharArray() : null;
                ks.load(in, passwordChars);
                return ks;
            } finally {
                if (in != null) {
                    in.close();
                }
            }
        }
    }
}

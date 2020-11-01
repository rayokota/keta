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
package io.kcache.keta.auth;

import com.google.protobuf.ByteString;
import io.etcd.jetcd.api.Permission;
import io.etcd.jetcd.api.Role;
import io.etcd.jetcd.api.User;
import io.kcache.Cache;
import io.kcache.KafkaCache;
import io.kcache.KafkaCacheConfig;
import io.kcache.keta.KetaConfig;
import io.kcache.keta.kafka.serialization.KafkaProtobufSerde;
import io.kcache.utils.Caches;
import io.kcache.utils.InMemoryCache;
import org.apache.kafka.common.serialization.Serdes;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class KetaAuthManager implements Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(KetaAuthManager.class);

    private KetaConfig config;
    private TokenProvider tokenProvider;
    private Cache<String, String> auth;
    private Cache<String, User> authUsers;
    private Cache<String, Role> authRoles;
    private final AtomicBoolean initialized = new AtomicBoolean();

    public KetaAuthManager(KetaConfig config, Cache<String, String> auth) {
        this.config = config;
        this.tokenProvider = config.getTokenProvider();
        this.auth = auth;
    }

    public void init() {
        if (isAuthEnabled()) {
            initAuthStores();
        }
    }

    public boolean isAuthEnabled() {
        return Boolean.parseBoolean(auth.getOrDefault("authEnabled", "false"));
    }

    public boolean isInitialized() {
        return initialized.get();
    }

    private void initAuthStores() {
        Map<String, Object> configs = config.originals();
        String bootstrapServers = (String) configs.get(KafkaCacheConfig.KAFKACACHE_BOOTSTRAP_SERVERS_CONFIG);
        String groupId = (String) configs.getOrDefault(KafkaCacheConfig.KAFKACACHE_GROUP_ID_CONFIG, "keta-1");

        CompletableFuture<Void> authUsersFuture = CompletableFuture.runAsync(() ->
            authUsers = initAuthUsers(new HashMap<>(configs), bootstrapServers, groupId));
        CompletableFuture<Void> authRolesFuture = CompletableFuture.runAsync(() ->
            authRoles = initAuthRoles(new HashMap<>(configs), bootstrapServers, groupId));
        CompletableFuture.allOf(authUsersFuture, authRolesFuture).join();

        boolean isInitialized = initialized.compareAndSet(false, true);
        if (!isInitialized) {
            throw new IllegalStateException("Illegal state while initializing auth stores. Auth stores "
                + "was already initialized");
        }
    }

    private Cache<String, User> initAuthUsers(Map<String, Object> configs, String bootstrapServers, String groupId) {
        Cache<String, User> authUsers;
        if (bootstrapServers != null) {
            String topic = "_keta_auth";
            configs.put(KafkaCacheConfig.KAFKACACHE_TOPIC_CONFIG, topic);
            configs.put(KafkaCacheConfig.KAFKACACHE_GROUP_ID_CONFIG, groupId);
            configs.put(KafkaCacheConfig.KAFKACACHE_CLIENT_ID_CONFIG, groupId + "-" + topic);
            authUsers = new KafkaCache<>(
                new KafkaCacheConfig(configs), Serdes.String(), new KafkaProtobufSerde<>(User.class), null, new InMemoryCache<>());
        } else {
            authUsers = new InMemoryCache<>();
        }
        authUsers = Caches.concurrentCache(authUsers);
        authUsers.init();
        return authUsers;
    }

    private Cache<String, Role> initAuthRoles(Map<String, Object> configs, String bootstrapServers, String groupId) {
        Cache<String, Role> authRoles;
        if (bootstrapServers != null) {
            String topic = "_keta_auth";
            configs.put(KafkaCacheConfig.KAFKACACHE_TOPIC_CONFIG, topic);
            configs.put(KafkaCacheConfig.KAFKACACHE_GROUP_ID_CONFIG, groupId);
            configs.put(KafkaCacheConfig.KAFKACACHE_CLIENT_ID_CONFIG, groupId + "-" + topic);
            authRoles = new KafkaCache<>(
                new KafkaCacheConfig(configs), Serdes.String(), new KafkaProtobufSerde<>(Role.class), null, new InMemoryCache<>());
        } else {
            authRoles = new InMemoryCache<>();
        }
        authRoles = Caches.concurrentCache(authRoles);
        authRoles.init();
        return authRoles;
    }

    public void sync() {
        if (isAuthEnabled()) {
            syncAuthStores();
        }
    }

    public void syncAuthStores() {
        CompletableFuture<Void> authUsersFuture = CompletableFuture.runAsync(() -> authUsers.sync());
        CompletableFuture<Void> authRolesFuture = CompletableFuture.runAsync(() -> authRoles.sync());
        CompletableFuture.allOf(authUsersFuture, authRolesFuture).join();
    }

    public void enableAuth() {
        auth.put("authEnabled", "true");
        if (!isInitialized()) {
            initAuthStores();
        }
    }

    public void disableAuth() {
        auth.put("authEnabled", "false");
    }

    public String authenticate(String user, String password) {
        User u = authUsers.get(user);
        boolean matches = BCrypt.checkpw(password, u.getPassword().toStringUtf8());
        if (!matches) {
            throw new AuthenticationException(user);
        }
        return tokenProvider.assignToken(user);
    }

    public String getUserFromToken(String token) {
        return tokenProvider.getUser(token);
    }

    public void addUser(String user, String password) {
        String pw = BCrypt.hashpw(password, BCrypt.gensalt());
        authUsers.put(user, User.newBuilder()
            .setName(ByteString.copyFrom(user, StandardCharsets.UTF_8))
            .setPassword(ByteString.copyFrom(pw, StandardCharsets.UTF_8))
            .build());
    }

    public User getUser(String user) {
        return authUsers.get(user);
    }

    public Set<String> listUsers() {
        return authUsers.keySet();
    }

    public void deleteUser(String user) {
        authUsers.remove(user);
    }

    public void changePassword(String user, String password) {
        String pw = BCrypt.hashpw(password, BCrypt.gensalt());
        authUsers.put(user, User.newBuilder()
            .setName(ByteString.copyFrom(user, StandardCharsets.UTF_8))
            .setPassword(ByteString.copyFrom(pw, StandardCharsets.UTF_8))
            .build());
    }

    public void grantRole(String user, String role) {
        User u = authUsers.get(user);
        authUsers.put(user, User.newBuilder(u)
            .addRoles(role)
            .build());
    }

    public void revokeRole(String user, String role) {
        User u = authUsers.get(user);
        Set<String> roles = new HashSet<>(u.getRolesList());
        roles.remove(role);
        authUsers.put(user, User.newBuilder(u)
            .clearRoles()
            .addAllRoles(roles)
            .build());
    }

    public void addRole(String role) {
        authRoles.put(role, Role.newBuilder()
            .setName(ByteString.copyFrom(role, StandardCharsets.UTF_8))
            .build());
    }

    public Role getRole(String role) {
        return authRoles.get(role);
    }

    public Set<String> listRoles() {
        return authRoles.keySet();
    }

    public void deleteRole(String role) {
        authRoles.remove(role);
    }

    public void grantPermission(String role, Permission permission) {
        Role r = authRoles.get(role);
        authRoles.put(role, Role.newBuilder(r)
            .addKeyPermission(permission)
            .build());
    }

    public void revokePermission(String role, String key, String rangeEnd) {
        Role r = authRoles.get(role);
        Set<Permission> perms = new HashSet<>();
        for (Permission perm : r.getKeyPermissionList()) {
            if (!Objects.equals(perm.getKey().toStringUtf8(), key)
                || !Objects.equals(perm.getRangeEnd().toStringUtf8(), rangeEnd)) {
                perms.add(perm);
            }
        }
        authRoles.put(role, Role.newBuilder(r)
            .clearKeyPermission()
            .addAllKeyPermission(perms)
            .build());
    }

    @Override
    public void close() throws IOException {
        if (authRoles != null) {
            authRoles.close();
        }
        if (authUsers != null) {
            authUsers.close();
        }
    }
}

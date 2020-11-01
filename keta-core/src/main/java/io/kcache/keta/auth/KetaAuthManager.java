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
import io.kcache.keta.auth.exceptions.AuthNotEnabledException;
import io.kcache.keta.auth.exceptions.AuthenticationException;
import io.kcache.keta.auth.exceptions.InvalidAuthMgmtException;
import io.kcache.keta.auth.exceptions.RoleAlreadyExistsException;
import io.kcache.keta.auth.exceptions.RoleIsEmptyException;
import io.kcache.keta.auth.exceptions.RoleNotFoundException;
import io.kcache.keta.auth.exceptions.RootRoleNotFoundException;
import io.kcache.keta.auth.exceptions.RootUserNotFoundException;
import io.kcache.keta.auth.exceptions.UserAlreadyExistsException;
import io.kcache.keta.auth.exceptions.UserIsEmptyException;
import io.kcache.keta.auth.exceptions.UserNotFoundException;
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
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class KetaAuthManager {

    private static final Logger LOG = LoggerFactory.getLogger(KetaAuthManager.class);

    public static final String ROOT_USER = "root";
    public static final String ROOT_ROLE = "root";

    private KetaConfig config;
    private TokenProvider tokenProvider;
    private Cache<String, String> auth;
    private Cache<String, User> authUsers;
    private Cache<String, Role> authRoles;

    public KetaAuthManager(KetaConfig config, Cache<String, String> auth,
                           Cache<String, User> authUsers, Cache<String, Role> authRoles) {
        this.config = config;
        this.tokenProvider = config.getTokenProvider();
        this.auth = auth;
        this.authUsers = authUsers;
        this.authRoles = authRoles;
    }

    public boolean isAuthEnabled() {
        boolean authEnabled = Boolean.parseBoolean(auth.getOrDefault("authEnabled", "false"));
        return authEnabled;
    }

    public void enableAuth() {
        User u = authUsers.get(ROOT_USER);
        if (u == null) {
            throw new RootUserNotFoundException(ROOT_USER);
        }
        if (!hasRootRole(u)) {
            throw new RootRoleNotFoundException(ROOT_ROLE);
        }
        auth.put("authEnabled", "true");
    }

    private boolean hasRootRole(User u) {
        for (String r : u.getRolesList()) {
            if (ROOT_ROLE.equals(r)) {
                return true;
            }
        }
        return false;
    }

    public void disableAuth() {
        auth.put("authEnabled", "false");
    }

    public String authenticate(String user, String password) {
        if (!isAuthEnabled()) {
            throw new AuthNotEnabledException(user);
        }
        User u = authUsers.get(user);
        if (u == null) {
            throw new AuthenticationException(user);
        }
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
        if (user.isEmpty()) {
            throw new UserIsEmptyException(user);
        }
        User u = authUsers.get(user);
        if (u != null) {
            throw new UserAlreadyExistsException(user);
        }
        String pw = BCrypt.hashpw(password, BCrypt.gensalt());
        authUsers.put(user, User.newBuilder()
            .setName(ByteString.copyFrom(user, StandardCharsets.UTF_8))
            .setPassword(ByteString.copyFrom(pw, StandardCharsets.UTF_8))
            .build());
    }

    public User getUser(String user) {
        User u = authUsers.get(user);
        if (u == null) {
            throw new UserNotFoundException(user);
        }
        return u;
    }

    public Set<String> listUsers() {
        return authUsers.keySet();
    }

    public void deleteUser(String user) {
        if (isAuthEnabled() && ROOT_USER.equals(user)) {
            LOG.error("cannot delete 'root' user");
            throw new InvalidAuthMgmtException();
        }
        User u = authUsers.remove(user);
        if (u == null) {
            throw new UserNotFoundException(user);
        }
    }

    public void changePassword(String user, String password) {
        if (user.isEmpty()) {
            throw new UserIsEmptyException(user);
        }
        User u = authUsers.get(user);
        if (u == null) {
            throw new UserNotFoundException(user);
        }
        String pw = BCrypt.hashpw(password, BCrypt.gensalt());
        authUsers.put(user, User.newBuilder()
            .setName(ByteString.copyFrom(user, StandardCharsets.UTF_8))
            .setPassword(ByteString.copyFrom(pw, StandardCharsets.UTF_8))
            .build());
    }

    public void grantRole(String user, String role) {
        User u = authUsers.get(user);
        if (u == null) {
            throw new UserNotFoundException(user);
        }
        authUsers.put(user, User.newBuilder(u)
            .addRoles(role)
            .build());
    }

    public void revokeRole(String user, String role) {
        if (isAuthEnabled() && ROOT_USER.equals(user) && ROOT_ROLE.equals(role)) {
            LOG.error("'root' user cannot revoke 'root' role");
            throw new InvalidAuthMgmtException();
        }
        User u = authUsers.get(user);
        if (u == null) {
            throw new UserNotFoundException(user);
        }
        Set<String> roles = new HashSet<>(u.getRolesList());
        roles.remove(role);
        authUsers.put(user, User.newBuilder(u)
            .clearRoles()
            .addAllRoles(roles)
            .build());
    }

    public void addRole(String role) {
        if (role.isEmpty()) {
            throw new RoleIsEmptyException(role);
        }
        Role r = authRoles.get(role);
        if (r != null) {
            throw new RoleAlreadyExistsException(role);
        }
        authRoles.put(role, Role.newBuilder()
            .setName(ByteString.copyFrom(role, StandardCharsets.UTF_8))
            .build());
    }

    public Role getRole(String role) {
        Role r = authRoles.get(role);
        if (r == null) {
            throw new RoleNotFoundException(role);
        }
        return r;
    }

    public Set<String> listRoles() {
        return authRoles.keySet();
    }

    public void deleteRole(String role) {
        if (isAuthEnabled() && ROOT_ROLE.equals(role)) {
            LOG.error("cannot delete 'root' role");
            throw new InvalidAuthMgmtException();
        }
        Role r = authRoles.remove(role);
        if (r == null) {
            throw new RoleNotFoundException(role);
        }
    }

    public void grantPermission(String role, Permission permission) {
        Role r = authRoles.get(role);
        if (r == null) {
            throw new RoleNotFoundException(role);
        }
        authRoles.put(role, Role.newBuilder(r)
            .addKeyPermission(permission)
            .build());
    }

    public void revokePermission(String role, ByteString key, ByteString rangeEnd) {
        Role r = authRoles.get(role);
        if (r == null) {
            throw new RoleNotFoundException(role);
        }
        Set<Permission> perms = new HashSet<>();
        for (Permission perm : r.getKeyPermissionList()) {
            if (!Objects.equals(perm.getKey(), key) || !Objects.equals(perm.getRangeEnd(), rangeEnd)) {
                perms.add(perm);
            }
        }
        authRoles.put(role, Role.newBuilder(r)
            .clearKeyPermission()
            .addAllKeyPermission(perms)
            .build());
    }
}

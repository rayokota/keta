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

import com.google.common.collect.Range;
import com.google.protobuf.ByteString;
import io.etcd.jetcd.api.Permission;
import io.etcd.jetcd.api.Role;
import io.etcd.jetcd.api.User;
import io.kcache.Cache;
import io.kcache.keta.KetaConfig;
import io.kcache.keta.auth.exceptions.AuthNotEnabledException;
import io.kcache.keta.auth.exceptions.AuthenticationException;
import io.kcache.keta.auth.exceptions.InvalidAuthMgmtException;
import io.kcache.keta.auth.exceptions.PermissionDeniedException;
import io.kcache.keta.auth.exceptions.RoleAlreadyExistsException;
import io.kcache.keta.auth.exceptions.RoleIsEmptyException;
import io.kcache.keta.auth.exceptions.RoleNotFoundException;
import io.kcache.keta.auth.exceptions.RootRoleNotFoundException;
import io.kcache.keta.auth.exceptions.RootUserNotFoundException;
import io.kcache.keta.auth.exceptions.UserAlreadyExistsException;
import io.kcache.keta.auth.exceptions.UserIsEmptyException;
import io.kcache.keta.auth.exceptions.UserNotFoundException;
import io.kcache.keta.utils.IntervalTree;
import org.apache.kafka.common.utils.Bytes;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class KetaAuthManager {

    private static final Logger LOG = LoggerFactory.getLogger(KetaAuthManager.class);

    public static final String ROOT_USER = "root";
    public static final String ROOT_ROLE = "root";

    private final KetaConfig config;
    private final TokenProvider tokenProvider;
    private final Cache<String, String> auth;
    private final Cache<String, User> authUsers;
    private final Cache<String, Role> authRoles;
    private final Map<String, UnifiedRangePerms> rangePerms;

    public KetaAuthManager(KetaConfig config, Cache<String, String> auth,
                           Cache<String, User> authUsers, Cache<String, Role> authRoles) {
        this.config = config;
        this.tokenProvider = config.getTokenProvider();
        this.auth = auth;
        this.authUsers = authUsers;
        this.authRoles = authRoles;
        this.rangePerms = new ConcurrentHashMap<>();
    }

    public boolean isAuthEnabled() {
        return Boolean.parseBoolean(auth.getOrDefault("authEnabled", "false"));
    }

    public void enableAuth() {
        if (isAuthEnabled()) {
            return;
        }
        User u = authUsers.get(ROOT_USER);
        if (u == null) {
            throw new RootUserNotFoundException(ROOT_USER);
        }
        if (!hasRootRole(u)) {
            throw new RootRoleNotFoundException(ROOT_ROLE);
        }
        auth.put("authEnabled", Boolean.TRUE.toString());
        LOG.info("enabled authentication");
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
        if (!isAuthEnabled()) {
            return;
        }
        auth.put("authEnabled", Boolean.FALSE.toString());
        LOG.info("disabled authentication");
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
        if (user == null || user.isEmpty()) {
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
        LOG.info("added user {}", user);
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
        rangePerms.remove(user);
        LOG.info("deleted a user {}", user);
    }

    public void changePassword(String user, String password) {
        if (user == null || user.isEmpty()) {
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
        rangePerms.remove(user);
        LOG.info("changed password of user {}", user);
    }

    public void grantRole(String user, String role) {
        User u = authUsers.get(user);
        if (u == null) {
            throw new UserNotFoundException(user);
        }
        if (!ROOT_ROLE.equals(role)) {
            Role r = authRoles.get(role);
            if (r == null) {
                throw new RoleNotFoundException(role);
            }
        }
        List<String> roles = u.getRolesList();
        if (!roles.contains(role)) {
            authUsers.put(user, User.newBuilder(u)
                .addRoles(role)
                .build());
            rangePerms.remove(user);
        }
        LOG.info("granted role to user {}", user);
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
        Set<String> roles = new LinkedHashSet<>(u.getRolesList());
        if (roles.remove(role)) {
            authUsers.put(user, User.newBuilder(u)
                .clearRoles()
                .addAllRoles(roles)
                .build());
            rangePerms.remove(user);
        }
        LOG.info("revoked role from user {}", user);
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
        LOG.info("created role {}", role);
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
        for (String user : authUsers.keySet()) {
            User u = authUsers.get(user);
            Set<String> roles = new LinkedHashSet<>(u.getRolesList());
            if (roles.remove(role)) {
                User newUser = User.newBuilder(u)
                    .clearRoles()
                    .addAllRoles(roles)
                    .build();
                authUsers.put(user, newUser);
                rangePerms.remove(user);
            }
        }
        LOG.info("deleted role {}", role);
    }

    public void grantPermission(String role, Permission permission) {
        Role r = authRoles.get(role);
        if (r == null) {
            throw new RoleNotFoundException(role);
        }
        List<Permission> perms = new ArrayList<>();
        for (Permission p : r.getKeyPermissionList()) {
            if (Objects.equals(p.getKey(), permission.getKey()) && Objects.equals(p.getRangeEnd(), permission.getRangeEnd())) {
                perms.add(permission);
            } else {
                perms.add(p);
            }
        }
        authRoles.put(role, Role.newBuilder(r)
            .clearKeyPermission()
            .addAllKeyPermission(perms)
            .build());
        rangePerms.clear();
        LOG.info("granted permission to role {}", role);
    }

    public void revokePermission(String role, ByteString key, ByteString rangeEnd) {
        Role r = authRoles.get(role);
        if (r == null) {
            throw new RoleNotFoundException(role);
        }
        List<Permission> perms = new ArrayList<>();
        for (Permission perm : r.getKeyPermissionList()) {
            if (!Objects.equals(perm.getKey(), key) || !Objects.equals(perm.getRangeEnd(), rangeEnd)) {
                perms.add(perm);
            }
        }
        authRoles.put(role, Role.newBuilder(r)
            .clearKeyPermission()
            .addAllKeyPermission(perms)
            .build());
        rangePerms.clear();
        LOG.info("revoked permission for role {}", role);
    }

    public void checkOpPermitted(String user, ByteString key, ByteString rangeEnd, Permission.Type type) {
        if (!isAuthEnabled()) {
            return;
        }
        if (user == null || user.isEmpty()) {
            throw new UserIsEmptyException(user);
        }
        User u = authUsers.get(user);
        if (u == null) {
            throw new PermissionDeniedException(user);
        }
        if (hasRootRole(u)) {
            return;
        }
        if (!isRangeOpPermitted(u, key, rangeEnd, type)) {
            throw new PermissionDeniedException(user);
        }
    }

    private boolean isRangeOpPermitted(User user, ByteString key, ByteString rangeEnd, Permission.Type type) {
        UnifiedRangePerms perms = this.rangePerms.computeIfAbsent(user.getName().toStringUtf8(), this::getMergedPerms);
        if (rangeEnd == null || rangeEnd.size() == 0) {
            return checkKeyPoint(perms, key, type);
        } else {
            return checkKeyInterval(perms, key, rangeEnd, type);
        }
    }

    private boolean checkKeyPoint(UnifiedRangePerms perms, ByteString key, Permission.Type type) {
        Range<Bytes> pt = Range.singleton(Bytes.wrap(key.toByteArray()));
        switch (type) {
            case READ:
                return perms.getReadPerms().overlappers(pt).hasNext();
            case WRITE:
                return perms.getWritePerms().overlappers(pt).hasNext();
            default:
                throw new IllegalArgumentException("Unknown auth type " + type);
        }
    }

    private boolean checkKeyInterval(UnifiedRangePerms perms, ByteString key, ByteString rangeEnd, Permission.Type type) {
        Bytes k = Bytes.wrap(key.toByteArray());
        Range<Bytes> interval;
        if (rangeEnd.size() == 1 && rangeEnd.byteAt(0) == 0) {
            interval = Range.atLeast(k);
        } else {
            interval = Range.closed(k, Bytes.wrap(rangeEnd.toByteArray()));
        }
        switch (type) {
            case READ:
                return perms.getReadPerms().overlappers(interval).hasNext();
            case WRITE:
                return perms.getWritePerms().overlappers(interval).hasNext();
            default:
                throw new IllegalArgumentException("Unknown auth type " + type);
        }
    }

    private UnifiedRangePerms getMergedPerms(String user) {
        User u = authUsers.get(user);
        if (u == null) {
            throw new PermissionDeniedException(user);
        }
        UnifiedRangePerms perms = new UnifiedRangePerms();
        for (String role : u.getRolesList()) {
            Role r = authRoles.get(role);
            if (r == null) {
                continue;
            }
            for (Permission p : r.getKeyPermissionList()) {
                Bytes key = Bytes.wrap(p.getKey().toByteArray());
                ByteString end = p.getRangeEnd();
                Range<Bytes> interval;
                if (end == null || end.size() == 0) {
                    interval = Range.singleton(key);
                } else if (end.size() == 1 && end.byteAt(0) == 0) {
                    interval = Range.atLeast(key);
                } else {
                    interval = Range.closed(key, Bytes.wrap(end.toByteArray()));
                }
                switch (p.getPermType()) {
                    case READWRITE:
                        perms.getReadPerms().put(interval, null);
                        perms.getWritePerms().put(interval, null);
                        break;
                    case READ:
                        perms.getReadPerms().put(interval, null);
                        break;
                    case WRITE:
                        perms.getWritePerms().put(interval, null);
                        break;
                }
            }
        }
        return perms;
    }

    static class UnifiedRangePerms {
        private final IntervalTree<Bytes, Void> readPerms;
        private final IntervalTree<Bytes, Void> writePerms;

        public UnifiedRangePerms() {
            this.readPerms = new IntervalTree<>();
            this.writePerms = new IntervalTree<>();
        }

        public IntervalTree<Bytes, Void> getReadPerms() {
            return readPerms;
        }

        public IntervalTree<Bytes, Void> getWritePerms() {
            return writePerms;
        }
    }
}

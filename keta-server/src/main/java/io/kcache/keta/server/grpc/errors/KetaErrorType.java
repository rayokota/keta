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
package io.kcache.keta.server.grpc.errors;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import static io.grpc.Status.Code.DATA_LOSS;
import static io.grpc.Status.Code.FAILED_PRECONDITION;
import static io.grpc.Status.Code.INVALID_ARGUMENT;
import static io.grpc.Status.Code.NOT_FOUND;
import static io.grpc.Status.Code.OUT_OF_RANGE;
import static io.grpc.Status.Code.PERMISSION_DENIED;
import static io.grpc.Status.Code.RESOURCE_EXHAUSTED;
import static io.grpc.Status.Code.UNAUTHENTICATED;
import static io.grpc.Status.Code.UNAVAILABLE;

public enum KetaErrorType {
    
    // Errors adapted from etcd

    EmptyKey      (INVALID_ARGUMENT, "key is not provided"),
    KeyNotFound   (INVALID_ARGUMENT, "key not found"),
    ValueProvided (INVALID_ARGUMENT, "value is provided"),
    LeaseProvided (INVALID_ARGUMENT, "lease is provided"),
    TooManyOps    (INVALID_ARGUMENT, "too many operations in txn request"),
    DuplicateKey  (INVALID_ARGUMENT, "duplicate key given in txn request"),
    Compacted     (OUT_OF_RANGE, "mvcc: required revision has been compacted"),
    FutureRev     (OUT_OF_RANGE, "mvcc: required revision is a future revision"),
    NoSpace       (RESOURCE_EXHAUSTED, "mvcc: database space exceeded"),

    LeaseNotFound    (NOT_FOUND, "requested lease not found"),
    LeaseExist       (FAILED_PRECONDITION, "lease already exists"),
    LeaseTTLTooLarge (OUT_OF_RANGE, "too large lease TTL"),

    MemberExist            (FAILED_PRECONDITION, "member ID already exist"),
    PeerURLExist           (FAILED_PRECONDITION, "Peer URLs already exists"),
    MemberNotEnoughStarted (FAILED_PRECONDITION, "re-configuration failed due to not enough started members"),
    MemberBadURLs          (INVALID_ARGUMENT, "given member URLs are invalid"),
    MemberNotFound         (NOT_FOUND, "member not found"),
    MemberNotLearner       (FAILED_PRECONDITION, "can only promote a learner member"),
    LearnerNotReady        (FAILED_PRECONDITION, "can only promote a learner member which is in sync with leader"),
    TooManyLearners        (FAILED_PRECONDITION, "too many learner members in cluster"),

    RequestTooLarge        (INVALID_ARGUMENT, "request is too large"),
    RequestTooManyRequests (RESOURCE_EXHAUSTED, "too many requests"),

    RootUserNotExist     (FAILED_PRECONDITION, "root user does not exist"),
    RootRoleNotExist     (FAILED_PRECONDITION, "root user does not have root role"),
    UserAlreadyExist     (FAILED_PRECONDITION, "user name already exists"),
    UserEmpty            (INVALID_ARGUMENT, "user name is empty"),
    UserNotFound         (FAILED_PRECONDITION, "user name not found"),
    RoleAlreadyExist     (FAILED_PRECONDITION, "role name already exists"),
    RoleNotFound         (FAILED_PRECONDITION, "role name not found"),
    RoleEmpty            (INVALID_ARGUMENT, "role name is empty"),
    AuthFailed           (INVALID_ARGUMENT, "authentication failed, invalid user ID or password"),
    PermissionDenied     (PERMISSION_DENIED, "permission denied"),
    RoleNotGranted       (FAILED_PRECONDITION, "role is not granted to the user"),
    PermissionNotGranted (FAILED_PRECONDITION, "permission is not granted to the role"),
    AuthNotEnabled       (FAILED_PRECONDITION, "authentication is not enabled"),
    InvalidAuthToken     (UNAUTHENTICATED, "invalid auth token"),
    InvalidAuthMgmt      (INVALID_ARGUMENT, "invalid auth management"),

    NoLeader                   (UNAVAILABLE, "no leader"),
    NotLeader                  (FAILED_PRECONDITION, "not leader"),
    LeaderChanged              (UNAVAILABLE, "leader changed"),
    NotCapable                 (UNAVAILABLE, "not capable"),
    Stopped                    (UNAVAILABLE, "server stopped"),
    Timeout                    (UNAVAILABLE, "request timed out"),
    TimeoutDueToLeaderFail     (UNAVAILABLE, "request timed out, possibly due to previous leader failure"),
    TimeoutDueToConnectionLost (UNAVAILABLE, "request timed out, possibly due to connection lost"),
    Unhealthy                  (UNAVAILABLE, "unhealthy cluster"),
    Corrupt                    (DATA_LOSS, "corrupt cluster"),
    ErrGPRCNotSupportedForLearner     (UNAVAILABLE, "rpc not supported for learner"),
    BadLeaderTransferee        (FAILED_PRECONDITION, "bad leader transferee"),

    ClusterVersionUnavailable     (UNAVAILABLE, "cluster version not found during downgrade"),
    WrongDowngradeVersionFormat   (INVALID_ARGUMENT, "wrong downgrade target version format"),
    InvalidDowngradeTargetVersion (INVALID_ARGUMENT, "invalid downgrade target version"),
    DowngradeInProcess            (FAILED_PRECONDITION, "cluster has a downgrade job in progress"),
    NoInflightDowngrade           (FAILED_PRECONDITION, "no inflight downgrade job");

    private Status.Code code;
    private String description;

    KetaErrorType(Status.Code code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public Status.Code getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public StatusRuntimeException toException() {
        return code.toStatus().withDescription(description).asRuntimeException();
    }
}
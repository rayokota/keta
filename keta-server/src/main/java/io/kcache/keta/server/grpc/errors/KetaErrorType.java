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

    EmptyKey      (INVALID_ARGUMENT, "ketaserver: key is not provided"),
    KeyNotFound   (INVALID_ARGUMENT, "ketaserver: key not found"),
    ValueProvided (INVALID_ARGUMENT, "ketaserver: value is provided"),
    LeaseProvided (INVALID_ARGUMENT, "ketaserver: lease is provided"),
    TooManyOps    (INVALID_ARGUMENT, "ketaserver: too many operations in txn request"),
    DuplicateKey  (INVALID_ARGUMENT, "ketaserver: duplicate key given in txn request"),
    Compacted     (OUT_OF_RANGE, "ketaserver: mvcc: required revision has been compacted"),
    FutureRev     (OUT_OF_RANGE, "ketaserver: mvcc: required revision is a future revision"),
    NoSpace       (RESOURCE_EXHAUSTED, "ketaserver: mvcc: database space exceeded"),

    LeaseNotFound    (NOT_FOUND, "ketaserver: requested lease not found"),
    LeaseExist       (FAILED_PRECONDITION, "ketaserver: lease already exists"),
    LeaseTTLTooLarge (OUT_OF_RANGE, "ketaserver: too large lease TTL"),

    MemberExist            (FAILED_PRECONDITION, "ketaserver: member ID already exist"),
    PeerURLExist           (FAILED_PRECONDITION, "ketaserver: Peer URLs already exists"),
    MemberNotEnoughStarted (FAILED_PRECONDITION, "ketaserver: re-configuration failed due to not enough started members"),
    MemberBadURLs          (INVALID_ARGUMENT, "ketaserver: given member URLs are invalid"),
    MemberNotFound         (NOT_FOUND, "ketaserver: member not found"),
    MemberNotLearner       (FAILED_PRECONDITION, "ketaserver: can only promote a learner member"),
    LearnerNotReady        (FAILED_PRECONDITION, "ketaserver: can only promote a learner member which is in sync with leader"),
    TooManyLearners        (FAILED_PRECONDITION, "ketaserver: too many learner members in cluster"),

    RequestTooLarge        (INVALID_ARGUMENT, "ketaserver: request is too large"),
    RequestTooManyRequests (RESOURCE_EXHAUSTED, "ketaserver: too many requests"),

    // TODO
    RootUserNotExist     (FAILED_PRECONDITION, "ketaserver: root user does not exist"),
    RootRoleNotExist     (FAILED_PRECONDITION, "ketaserver: root user does not have root role"),
    UserAlreadyExist     (FAILED_PRECONDITION, "ketaserver: user name already exists"),
    UserEmpty            (INVALID_ARGUMENT, "ketaserver: user name is empty"),
    UserNotFound         (FAILED_PRECONDITION, "ketaserver: user name not found"),
    RoleAlreadyExist     (FAILED_PRECONDITION, "ketaserver: role name already exists"),
    RoleNotFound         (FAILED_PRECONDITION, "ketaserver: role name not found"),
    RoleEmpty            (INVALID_ARGUMENT, "ketaserver: role name is empty"),
    AuthFailed           (INVALID_ARGUMENT, "ketaserver: authentication failed, invalid user ID or password"),
    PermissionDenied     (PERMISSION_DENIED, "ketaserver: permission denied"),
    RoleNotGranted       (FAILED_PRECONDITION, "ketaserver: role is not granted to the user"),
    PermissionNotGranted (FAILED_PRECONDITION, "ketaserver: permission is not granted to the role"),
    AuthNotEnabled       (FAILED_PRECONDITION, "ketaserver: authentication is not enabled"),
    InvalidAuthToken     (UNAUTHENTICATED, "ketaserver: invalid auth token"),
    InvalidAuthMgmt      (INVALID_ARGUMENT, "ketaserver: invalid auth management"),

    NoLeader                   (UNAVAILABLE, "ketaserver: no leader"),
    NotLeader                  (FAILED_PRECONDITION, "ketaserver: not leader"),
    LeaderChanged              (UNAVAILABLE, "ketaserver: leader changed"),
    NotCapable                 (UNAVAILABLE, "ketaserver: not capable"),
    Stopped                    (UNAVAILABLE, "ketaserver: server stopped"),
    Timeout                    (UNAVAILABLE, "ketaserver: request timed out"),
    TimeoutDueToLeaderFail     (UNAVAILABLE, "ketaserver: request timed out, possibly due to previous leader failure"),
    TimeoutDueToConnectionLost (UNAVAILABLE, "ketaserver: request timed out, possibly due to connection lost"),
    Unhealthy                  (UNAVAILABLE, "ketaserver: unhealthy cluster"),
    Corrupt                    (DATA_LOSS, "ketaserver: corrupt cluster"),
    ErrGPRCNotSupportedForLearner     (UNAVAILABLE, "ketaserver: rpc not supported for learner"),
    BadLeaderTransferee        (FAILED_PRECONDITION, "ketaserver: bad leader transferee"),

    ClusterVersionUnavailable     (UNAVAILABLE, "ketaserver: cluster version not found during downgrade"),
    WrongDowngradeVersionFormat   (INVALID_ARGUMENT, "ketaserver: wrong downgrade target version format"),
    InvalidDowngradeTargetVersion (INVALID_ARGUMENT, "ketaserver: invalid downgrade target version"),
    DowngradeInProcess            (FAILED_PRECONDITION, "ketaserver: cluster has a downgrade job in progress"),
    NoInflightDowngrade           (FAILED_PRECONDITION, "ketaserver: no inflight downgrade job"),

    // Added
    Starting                   (UNAVAILABLE, "ketaserver: server starting");

    private final Status.Code code;
    private final String description;

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
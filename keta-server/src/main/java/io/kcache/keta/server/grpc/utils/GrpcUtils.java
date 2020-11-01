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
package io.kcache.keta.server.grpc.utils;

import io.etcd.jetcd.api.ResponseHeader;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.kcache.keta.auth.exceptions.AuthNotEnabledException;
import io.kcache.keta.auth.exceptions.AuthenticationException;
import io.kcache.keta.auth.exceptions.InvalidAuthMgmtException;
import io.kcache.keta.auth.exceptions.InvalidAuthTokenException;
import io.kcache.keta.auth.exceptions.RoleAlreadyExistsException;
import io.kcache.keta.auth.exceptions.RoleIsEmptyException;
import io.kcache.keta.auth.exceptions.RoleNotFoundException;
import io.kcache.keta.auth.exceptions.UserAlreadyExistsException;
import io.kcache.keta.auth.exceptions.UserIsEmptyException;
import io.kcache.keta.auth.exceptions.UserNotFoundException;
import io.kcache.keta.lease.exceptions.LeaseExistsException;
import io.kcache.keta.lease.exceptions.LeaseNotFoundException;
import io.kcache.keta.server.grpc.errors.KetaErrorType;
import io.kcache.keta.server.grpc.errors.KetaException;
import io.kcache.keta.version.exceptions.KeyNotFoundException;

public class GrpcUtils {

    public static ResponseHeader toResponseHeader(int memberId) {
        return ResponseHeader.newBuilder()
            .setMemberId(memberId)
            .build();
    }

    public static ResponseHeader toResponseHeader(int memberId, long revision) {
        return ResponseHeader.newBuilder()
            .setMemberId(memberId)
            .setRevision(revision)
            .build();
    }

    public static StatusRuntimeException toStatusException(Exception ex) {
        if (ex instanceof AuthenticationException) {
            return KetaErrorType.AuthFailed.toException();
        } else if (ex instanceof AuthNotEnabledException) {
            return KetaErrorType.AuthNotEnabled.toException();
        } else if (ex instanceof InvalidAuthTokenException) {
            return KetaErrorType.InvalidAuthToken.toException();
        } else if (ex instanceof InvalidAuthMgmtException) {
            return KetaErrorType.InvalidAuthMgmt.toException();
        } else if (ex instanceof RoleAlreadyExistsException) {
            return KetaErrorType.RoleAlreadyExist.toException();
        } else if (ex instanceof RoleIsEmptyException) {
            return KetaErrorType.RoleEmpty.toException();
        } else if (ex instanceof RoleNotFoundException) {
            return KetaErrorType.RoleNotFound.toException();
        } else if (ex instanceof UserAlreadyExistsException) {
            return KetaErrorType.UserAlreadyExist.toException();
        } else if (ex instanceof UserIsEmptyException) {
            return KetaErrorType.UserEmpty.toException();
        } else if (ex instanceof UserNotFoundException) {
            return KetaErrorType.UserNotFound.toException();
        } else if (ex instanceof KeyNotFoundException) {
            return KetaErrorType.KeyNotFound.toException();
        } else if (ex instanceof LeaseExistsException) {
            return KetaErrorType.LeaseExist.toException();
        } else if (ex instanceof LeaseNotFoundException) {
            return KetaErrorType.LeaseNotFound.toException();
        } else if (ex instanceof KetaException) {
            return ((KetaException) ex).getType().toException();
        } else {
            return Status.UNKNOWN.withDescription(ex.getMessage()).asRuntimeException();
        }
    }
}
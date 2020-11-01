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
import io.kcache.keta.auth.AuthenticationException;
import io.kcache.keta.lease.LeaseExistsException;
import io.kcache.keta.lease.LeaseNotFoundException;
import io.kcache.keta.server.grpc.errors.KetaErrorType;
import io.kcache.keta.server.grpc.errors.KetaException;
import io.kcache.keta.version.KeyNotFoundException;

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
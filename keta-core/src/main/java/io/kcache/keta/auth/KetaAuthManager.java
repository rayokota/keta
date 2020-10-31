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

import io.kcache.Cache;
import io.kcache.keta.KetaEngine;
import io.kcache.keta.lease.Lease;
import io.kcache.keta.lease.LeaseExistsException;
import io.kcache.keta.lease.LeaseKeys;
import io.kcache.keta.lease.LeaseNotFoundException;
import io.kcache.keta.version.TxVersionedCache;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.apache.kafka.common.utils.Bytes;
import org.apache.omid.transaction.RollbackException;
import org.apache.omid.transaction.Transaction;
import org.apache.omid.transaction.TransactionException;
import org.apache.omid.transaction.TransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class KetaAuthManager {

    private static final Logger LOG = LoggerFactory.getLogger(KetaAuthManager.class);

    private TokenProvider tokenProvider;

    public KetaAuthManager() {
    }

}

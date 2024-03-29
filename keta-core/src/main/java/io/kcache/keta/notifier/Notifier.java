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

package io.kcache.keta.notifier;

import io.etcd.jetcd.api.Event;
import io.kcache.CacheUpdateHandler;
import io.kcache.keta.version.VersionedValues;

/**
 * Notifier are used to pass watched keys between KVService and WatchService
 */
public interface Notifier extends CacheUpdateHandler<byte[], VersionedValues> {
    void publish(long watchID, Event event);

    void watch(long watchID, Handler<Event> handler);

    void unwatch(long watchID);
}

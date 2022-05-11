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
import io.kcache.keta.KetaEngine;
import io.kcache.keta.pb.VersionedValue;
import io.kcache.keta.utils.ProtoUtils;
import io.kcache.keta.version.VersionedValues;
import io.kcache.keta.watch.KetaWatchManager;
import io.kcache.keta.watch.Watch;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static io.kcache.keta.version.TxVersionedCache.INVALID_TX;
import static io.kcache.keta.version.TxVersionedCache.PENDING_TX;

public abstract class AbstractNotifier implements Notifier {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractNotifier.class);

    private volatile int maxGenerationId = -1;

    @Override
    public abstract void publish(long watchID, Event event);

    @Override
    public abstract void watch(long watchID, Handler<Event> handler);

    @Override
    public abstract void unwatch(long watchID);

    @Override
    public ValidationStatus validateUpdate(byte[] key, VersionedValues value, TopicPartition tp, long offset, long timestamp) {
        if (value == null) {
            return ValidationStatus.SUCCESS;
        }
        int generationId = value.getGenerationId();
        if (generationId < maxGenerationId) {
            LOG.error("Value with generation {}, but max generation {}", generationId, maxGenerationId);
            return ValidationStatus.ROLLBACK_FAILURE;
        } else if (generationId > maxGenerationId) {
            maxGenerationId = generationId;
        }
        return ValidationStatus.SUCCESS;
    }

    @Override
    public void handleUpdate(byte[] key, VersionedValues value, VersionedValues oldValue, TopicPartition tp, long offset, long timestamp) {
        if (value == null) {
            return;
        }
        Iterator<Map.Entry<Long, VersionedValue>> newValues = value.getValues().descendingMap().entrySet().iterator();
        if (!newValues.hasNext()) {
            return;
        }
        VersionedValue currValue = newValues.next().getValue();
        long currCommit = currValue.getCommit();
        if (currCommit == INVALID_TX || currCommit == PENDING_TX) {
            return;
        }
        VersionedValue prevValue = null;
        if (oldValue != null) {
            for (Map.Entry<Long, VersionedValue> entry : oldValue.getValues().descendingMap().entrySet()) {
                VersionedValue val = entry.getValue();
                long commit = val.getCommit();
                if (commit == INVALID_TX || commit == PENDING_TX || commit == currCommit) {
                    continue;
                }
                prevValue = val;
            }
        }
        Event.Builder builder = Event.newBuilder();
        if (currValue.getDeleted()) {
            builder.setType(Event.EventType.DELETE)
                .setKv(ProtoUtils.toKeyValue(key, currValue));
        } else {
            builder.setType(Event.EventType.PUT)
                .setKv(ProtoUtils.toKeyValue(key, currValue));
        }
        if (prevValue != null && !prevValue.getDeleted()) {
            builder.setPrevKv(ProtoUtils.toKeyValue(key, prevValue));
        }
        KetaWatchManager watchMgr = KetaEngine.getInstance().getWatchManager();
        Set<Watch> watches = watchMgr.getWatches(key);
        for (Watch watch : watches) {
            publish(watch.getID(), builder.build());
        }
    }
}

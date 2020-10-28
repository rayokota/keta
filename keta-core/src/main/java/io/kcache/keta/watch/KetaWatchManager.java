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
package io.kcache.keta.watch;

import com.google.common.collect.Range;
import io.etcd.jetcd.api.Event;
import io.kcache.keta.notifier.Notifier;
import io.kcache.keta.utils.IntervalTree;
import io.vertx.core.Handler;
import org.apache.kafka.common.utils.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class KetaWatchManager {

    private static final Logger LOG = LoggerFactory.getLogger(KetaWatchManager.class);

    private final Notifier notifier;
    private final Map<Bytes, Set<Watch>> keyWatchers;
    private final IntervalTree<Bytes, Set<Watch>> ranges;
    private final Map<Long, Watch> watchers;

    public KetaWatchManager(Notifier notifier) {
        this.notifier = notifier;
        this.keyWatchers = new HashMap<>();
        this.ranges = new IntervalTree<>();
        this.watchers = new HashMap<>();
    }

    public synchronized Watch add(Watch watch) {
        long id = watch.getId();
        Bytes key = Bytes.wrap(watch.getKey());
        while (id == 0) {
            long newId = ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
            if (!watchers.containsKey(newId)) {
                watch = new Watch(newId, watch.getKey(), watch.getEnd());
                id = newId;
            }
        }
        if (watch.getEnd() != null && watch.getEnd().length > 0) {
            Bytes end = Bytes.wrap(watch.getEnd());
            Range<Bytes> interval = Range.closed(key, end);
            IntervalTree.Node<Bytes, Set<Watch>> node = ranges.find(interval);
            if (node != null) {
                node.getValue().add(watch);
            } else {
                Set<Watch> watches = new HashSet<>();
                watches.add(watch);
                ranges.put(interval, watches);
            }
        } else {
            Set<Watch> watches = keyWatchers.computeIfAbsent(key, k -> new HashSet<>());
            watches.add(watch);
        }
        watchers.put(watch.getId(), watch);
        return watch;
    }

    public void watch(Watch watch, Handler<Event> handler) {
        notifier.watch(watch.getId(), handler);
    }

    public synchronized Set<Watch> getWatches(byte[] key) {
        Bytes k = Bytes.wrap(key);
        Set<Watch> keys = keyWatchers.getOrDefault(k, Collections.emptySet());
        Iterator<IntervalTree.Node<Bytes, Set<Watch>>> iter = ranges.overlappers(Range.closed(k, k));
        List<Set<Watch>> ranges = new ArrayList<>();
        while (iter.hasNext()) {
            ranges.add(iter.next().getValue());
        }

        if (keys.isEmpty() && ranges.isEmpty()) {
            return Collections.emptySet();
        } else if (ranges.isEmpty()) {
            return keys;
        } else if (ranges.size() == 1) {
            return ranges.get(0);
        }

        Set<Watch> union = new HashSet<>(keys);
        for (Set<Watch> range : ranges) {
            union.addAll(range);
        }
        return union;
    }

    public synchronized boolean delete(long id) {
        Watch watch = watchers.remove(id);
        if (watch == null) {
            return false;
        }
        Bytes key = Bytes.wrap(watch.getKey());
        if (watch.getEnd() == null || watch.getEnd().length == 0) {
            Set<Watch> watches = keyWatchers.get(key);
            boolean removed = watches.remove(watch);
            if (watches.isEmpty()) {
                keyWatchers.remove(key);
            }
            return removed;
        }
        Bytes end = Bytes.wrap(watch.getEnd());
        Range<Bytes> interval = Range.closed(key, end);
        IntervalTree.Node<Bytes, Set<Watch>> node = ranges.find(interval);
        if (node != null) {
            Set<Watch> watches = node.getValue();
            boolean removed = watches.remove(watch);
            if (watches.isEmpty()) {
                ranges.remove(interval);
            }
            return removed;
        }
        return false;
    }

    public void unwatch(long id) {
        notifier.unwatch(id);
    }

}

package io.kcache.keta.notifier;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import io.etcd.jetcd.api.Event;
import io.etcd.jetcd.api.KeyValue;
import io.kcache.CacheUpdateHandler;
import io.kcache.keta.KetaEngine;
import io.kcache.keta.version.VersionedValue;
import io.kcache.keta.version.VersionedValues;
import io.kcache.keta.watch.KetaWatchManager;
import io.kcache.keta.watch.Watch;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static io.kcache.keta.version.TxVersionedCache.INVALID_TX;
import static io.kcache.keta.version.TxVersionedCache.PENDING_TX;

/**
 * A Notifier that use Vertx Event Bus
 */
public class KetaNotifier implements CacheUpdateHandler<byte[], VersionedValues>, Notifier {
    private static final Logger LOG = LoggerFactory.getLogger(KetaNotifier.class);

    // TODO switch to Guava?
    private final EventBus eventBus;

    private int maxGenerationId = -1;

    public KetaNotifier(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void publish(long watchID, Event event) {
        LOG.info("publishing to {}", watchID);
        this.eventBus.publish(String.valueOf(watchID), event.toByteArray());
    }

    public void watch(long watchID, Handler<Event> handler) {
        LOG.info("listening on {}", watchID);
        // TODO unregister consumer
        this.eventBus.consumer(String.valueOf(watchID), message -> {
            LOG.info("received a message from the eventbus: '{}'", message);
            if (message.body() instanceof byte[]) {
                try {
                    Event event = Event.newBuilder()
                        .mergeFrom((byte[]) message.body())
                        .build();
                    handler.handle(event);
                } catch (InvalidProtocolBufferException e) {
                    LOG.error("cannot create Event: '{}', skipping", e.toString());
                }
            } else {
                LOG.error("received a message wich is not byte[], skipping");
            }
        });

    }

    @Override
    public boolean validateUpdate(byte[] key, VersionedValues value, TopicPartition tp, long offset, long timestamp) {
        int generationId = value.getGenerationId();
        if (generationId < maxGenerationId) {
            return false;
        } else if (generationId > maxGenerationId) {
            maxGenerationId = generationId;
        }
        return true;
    }

    @Override
    public void handleUpdate(byte[] key, VersionedValues value, VersionedValues oldValue, TopicPartition tp, long offset, long timestamp) {
        KetaWatchManager watchMgr = KetaEngine.getInstance().getWatchManager();
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
            Iterator<Map.Entry<Long, VersionedValue>> oldValues = oldValue.getValues().descendingMap().entrySet().iterator();
            while (oldValues.hasNext()) {
                VersionedValue val = oldValues.next().getValue();
                long commit = val.getCommit();
                if (commit == INVALID_TX || commit == PENDING_TX || commit == currCommit) {
                    continue;
                }
                prevValue = val;
            }
        }
        Event.Builder builder = Event.newBuilder();
        if (currValue.isDeleted()) {
            builder.setType(Event.EventType.DELETE)
                .setKv(KeyValue.newBuilder()
                    .setKey(ByteString.copyFrom(key))
                    .build());
        } else {
            builder.setType(Event.EventType.PUT)
                .setKv(KeyValue.newBuilder()
                    .setKey(ByteString.copyFrom(key))
                    .setValue(ByteString.copyFrom(currValue.getValue()))
                    .build());
        }
        if (prevValue != null && !prevValue.isDeleted()) {
            builder.setPrevKv(KeyValue.newBuilder()
                .setKey(ByteString.copyFrom(key))
                .setValue(ByteString.copyFrom(prevValue.getValue()))
                .build());
        }
        Set<Watch> watches = watchMgr.getWatches(key);
        for (Watch watch : watches) {
            publish(watch.getId(), builder.build());
        }
    }
}

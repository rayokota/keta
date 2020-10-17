package io.kcache.keta.notifier;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import io.etcd.jetcd.api.Event;
import io.etcd.jetcd.api.KeyValue;
import io.kcache.CacheUpdateHandler;
import io.kcache.keta.KetaEngine;
import io.kcache.keta.version.TxVersionedCache;
import io.kcache.keta.version.VersionedValue;
import io.kcache.keta.version.VersionedValues;
import io.kcache.keta.watch.KetaWatchManager;
import io.kcache.keta.watch.Watch;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import org.apache.kafka.common.TopicPartition;
import org.apache.omid.transaction.RollbackException;
import org.apache.omid.transaction.Transaction;
import org.apache.omid.transaction.TransactionException;
import org.apache.omid.transaction.TransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

/**
 * A Notifier that use Vertx Event Bus
 */
public class KetaNotifier implements CacheUpdateHandler<byte[], VersionedValues>, Notifier {
    private static final Logger LOG = LoggerFactory.getLogger(KetaNotifier.class);

    private final EventBus eventBus;

    public KetaNotifier(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void publish(long watchID, Event event) {
        LOG.info("publishing to {}", watchID);
        this.eventBus.publish(String.valueOf(watchID), event.toByteArray());

    }

    public void watch(long watchID, Handler<Event> handler) {
        LOG.info("listening on {}", watchID);
        this.eventBus.consumer(String.valueOf(watchID), message -> {
            LOG.info("received a message from the eventbus: '{}'", message);
            if (message.body() instanceof byte[]) {
                try {
                    Event event = Event.newBuilder()
                        .mergeFrom((byte[]) message.body()).build();
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
        return true;
    }

    @Override
    public void handleUpdate(byte[] key, VersionedValues value, VersionedValues oldValue, TopicPartition tp, long offset, long timestamp) {
        KetaWatchManager watchMgr = KetaEngine.getInstance().getWatchManager();
        TransactionManager txMgr = KetaEngine.getInstance().getTxManager();
        TxVersionedCache txCache = KetaEngine.getInstance().getTxCache();
        Transaction tx = null;
        try {
            tx = txMgr.begin();
            List<VersionedValue> newValues = txCache.getVersions(key, value, true);
            if (newValues.isEmpty()) {
                return;
            }
            VersionedValue currValue = newValues.get(0);
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
            List<VersionedValue> oldValues = txCache.getVersions(key, oldValue, true);
            if (!oldValues.isEmpty()) {
                VersionedValue prevValue = oldValues.get(0);
                if (!prevValue.isDeleted()) {
                    builder.setPrevKv(KeyValue.newBuilder()
                        .setKey(ByteString.copyFrom(key))
                        .setValue(ByteString.copyFrom(prevValue.getValue()))
                        .build());
                }
            }
            Set<Watch> watches = watchMgr.getWatches(key);
            for (Watch watch : watches) {
                publish(watch.getId(), builder.build());
            }
            txMgr.commit(tx);
        } catch (TransactionException | RollbackException e) {
            if (tx != null) {
                try {
                    txMgr.rollback(tx);
                } catch (TransactionException te) {
                    // ignore
                }
            }
            // TODO
        }
    }
}

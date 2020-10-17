package io.kcache.keta.server;

import io.kcache.CacheUpdateHandler;
import io.kcache.keta.version.VersionedValues;
import io.vertx.core.eventbus.EventBus;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Notifier that use Vertx Event Bus
 */
public class KetaNotifier implements CacheUpdateHandler<byte[], VersionedValues> {
  private static final Logger log = LoggerFactory.getLogger(KetaNotifier.class);
  private final EventBus eventBus;

  public KetaNotifier(EventBus eventBus) {
    this.eventBus = eventBus;
  }

    @Override
    public boolean validateUpdate(byte[] key, VersionedValues value, TopicPartition tp, long offset, long timestamp) {
        return true;
    }

    @Override
    public void handleUpdate(byte[] key, VersionedValues value, VersionedValues oldValue, TopicPartition tp, long offset, long timestamp) {



    }

    /*
    @Override

  @Override
  public void publish(String tenant, long watchID, EtcdIoKvProto.Event event) {
    log.info("publishing to {}", tenant + watchID);
    this.eventBus.publish(tenant + watchID, event.toByteArray());
  }

  @Override
  public void watch(String tenant, long watchID, Handler<EtcdIoKvProto.Event> handler) {
    log.info("listening on {}", tenant + watchID);
    this.eventBus.consumer(tenant + watchID, message -> {
      log.info("received a message from the eventbus: '{}'", message);
      if (message.body() instanceof byte[]) {
        try {
          EtcdIoKvProto.Event event = EtcdIoKvProto.Event.newBuilder().mergeFrom((byte[]) message.body()).build();
          handler.handle(event);
        } catch (InvalidProtocolBufferException e) {
          log.error("cannot create Event: '{}', skipping", e.toString());
        }
      } else {
        log.error("received a message wich is not byte[], skipping");
      }
    });
  }

     */
}

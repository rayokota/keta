package io.kcache.keta.server.notifier;

import io.etcd.jetcd.api.Event;
import io.vertx.core.Handler;

/**
 * Notifier are used to pass watched keys between KVService and WatchService
 */
public interface Notifier {
  void publish(long watchID, Event event);

  void watch(long watchID, Handler<Event> handler);
}

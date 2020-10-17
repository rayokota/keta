package io.kcache.keta.server;

import io.kcache.keta.KetaConfig;
import io.kcache.keta.KetaEngine;
import io.kcache.keta.server.grpc.KVService;
import io.kcache.keta.server.grpc.LeaseService;
import io.kcache.keta.server.grpc.WatchService;
import io.kcache.keta.notifier.KetaNotifier;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.grpc.VertxServer;
import io.vertx.grpc.VertxServerBuilder;

public class KetaMain extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        VertxServerBuilder serverBuilder = VertxServerBuilder
            .forAddress(vertx,
                this.context.config().getString("listen-address", "localhost"),
                this.context.config().getInteger("listen-port", 8080))
            .addService(new KVService())
            .addService(new LeaseService())
            .addService(new WatchService());

        VertxServer server = serverBuilder.build();

        server.start(ar -> {
            if (ar.succeeded()) {
                System.out.println("gRPC service started");
                startPromise.complete();
            } else {
                System.out.println("Could not start server " + ar.cause().getMessage());
                startPromise.fail(ar.cause());
            }
        });
    }

    public static void main(String[] args) throws Exception {
        final KetaConfig config = new KetaConfig(args[0]);
        KetaEngine engine = KetaEngine.getInstance();
        engine.configure(config);
        Vertx vertx = Vertx.vertx();
        engine.init(new KetaNotifier(vertx.eventBus()));
        vertx.deployVerticle(new KetaMain());
    }
}

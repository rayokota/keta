package io.kcache.ketsie.server;

import io.kcache.ketsie.KetsieConfig;
import io.kcache.ketsie.KetsieEngine;
import io.kcache.ketsie.server.grpc.KVService;
import io.kcache.ketsie.server.grpc.LeaseService;
import io.kcache.ketsie.server.grpc.WatchService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.grpc.VertxServer;
import io.vertx.grpc.VertxServerBuilder;

public class KetsieMain extends AbstractVerticle {

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
        final KetsieConfig config = new KetsieConfig(args[0]);
        KetsieEngine engine = KetsieEngine.getInstance();
        engine.configure(config);
        engine.init();
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new KetsieMain());
    }
}

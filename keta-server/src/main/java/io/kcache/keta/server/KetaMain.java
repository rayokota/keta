package io.kcache.keta.server;

import io.grpc.ServerBuilder;
import io.grpc.netty.NettyServerBuilder;
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

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class KetaMain extends AbstractVerticle {
    private static final Logger LOG = Logger.getLogger(KetaMain.class.getName());

    private int port = 8080;

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        VertxServerBuilder serverBuilder = VertxServerBuilder
            .forAddress(vertx,
                this.context.config().getString("listen-address", "localhost"),
                this.context.config().getInteger("listen-port", port));

        NettyServerBuilder nettyBuilder = serverBuilder.nettyBuilder()
            .permitKeepAliveWithoutCalls(true)
            .permitKeepAliveTime(5, TimeUnit.SECONDS)
            .addService(new KVService())
            .addService(new LeaseService())
            .addService(new WatchService());

        VertxServer server = serverBuilder.build();

        server.start(ar -> {
            if (ar.succeeded()) {
                LOG.info("Server started, listening on " + port);
                LOG.info("Ketsie is at your service...");
                startPromise.complete();
            } else {
                LOG.info("Could not start server " + ar.cause().getLocalizedMessage());
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

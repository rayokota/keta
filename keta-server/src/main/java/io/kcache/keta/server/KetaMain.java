package io.kcache.keta.server;

import com.google.protobuf.Descriptors;
import io.etcd.jetcd.api.JetcdProto;
import io.etcd.jetcd.api.KVGrpc;
import io.grpc.CallOptions;
import io.grpc.netty.NettyServerBuilder;
import io.kcache.keta.KetaConfig;
import io.kcache.keta.KetaEngine;
import io.kcache.keta.server.grpc.KVService;
import io.kcache.keta.server.grpc.LeaseService;
import io.kcache.keta.server.grpc.WatchService;
import io.kcache.keta.notifier.KetaNotifier;
import io.kcache.keta.server.grpc.proxy.GrpcProxy;
import io.kcache.keta.server.leader.KetaIdentity;
import io.kcache.keta.server.leader.KetaLeaderElector;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.grpc.VertxServer;
import io.vertx.grpc.VertxServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class KetaMain extends AbstractVerticle {

    private static final Logger LOG = LoggerFactory.getLogger(KetaMain.class);

    private GrpcProxy proxy;
    private KetaIdentity identity;

    public KetaMain(GrpcProxy proxy, KetaIdentity identity) {
        this.proxy = proxy;
        this.identity = identity;
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        VertxServerBuilder serverBuilder = VertxServerBuilder
            .forAddress(vertx,
                this.context.config().getString("listen-address", identity.getHost()),
                this.context.config().getInteger("listen-port", identity.getPort()));

        Descriptors.ServiceDescriptor kv = JetcdProto.getDescriptor().findServiceByName("KV");
        NettyServerBuilder nettyBuilder = serverBuilder.nettyBuilder()
            .permitKeepAliveWithoutCalls(true)
            .permitKeepAliveTime(5, TimeUnit.SECONDS)
            .addService(proxy.buildServiceProxy(new KVService().bindService()))
            .addService(new LeaseService())
            .addService(new WatchService());

        VertxServer server = serverBuilder.build();

        server.start(ar -> {
            if (ar.succeeded()) {
                LOG.info("Server started, listening on " + identity.getPort());
                LOG.info("Ketsie is at your service...");
                startPromise.complete();
            } else {
                LOG.info("Could not start server " + ar.cause().getLocalizedMessage());
                startPromise.fail(ar.cause());
            }
        });
    }

    public static void main(String[] args) {
        try {
            if (args.length < 1) {
                LOG.error("Properties file is required to start");
                System.exit(1);
            }
            final KetaConfig config = new KetaConfig(args[0]);
            KetaEngine engine = KetaEngine.getInstance();
            engine.configure(config);
            Vertx vertx = Vertx.vertx();
            engine.init(new KetaNotifier(vertx.eventBus()));
            GrpcProxy proxy = new GrpcProxy(CallOptions.DEFAULT);
            LOG.info("Starting leader election...");
            KetaLeaderElector elector = new KetaLeaderElector(config, engine, proxy);
            elector.init();
            boolean isLeader = elector.isLeader();
            LOG.info("Leader: {}, starting server...", isLeader);
            vertx.deployVerticle(new KetaMain(proxy, elector.getIdentity()));
        } catch (Exception e) {
            LOG.error("Server died unexpectedly: ", e);
            System.exit(1);
        }
    }
}

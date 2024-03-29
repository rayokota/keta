package io.kcache.keta.server;

import io.grpc.ServerServiceDefinition;
import io.grpc.netty.NettyServerBuilder;
import io.kcache.keta.KetaConfig;
import io.kcache.keta.KetaEngine;
import io.kcache.keta.server.grpc.AuthService;
import io.kcache.keta.server.grpc.ClusterService;
import io.kcache.keta.server.grpc.KVService;
import io.kcache.keta.server.grpc.LeaseService;
import io.kcache.keta.server.grpc.MaintenanceService;
import io.kcache.keta.server.grpc.WatchService;
import io.kcache.keta.server.grpc.proxy.GrpcProxy;
import io.kcache.keta.server.grpc.utils.AuthServerInterceptor;
import io.kcache.keta.server.grpc.utils.SslFactory;
import io.kcache.keta.server.leader.KetaLeaderElector;
import io.kcache.keta.server.notifier.VertxNotifier;
import io.netty.channel.ChannelOption;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.grpc.VertxServer;
import io.vertx.grpc.VertxServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class KetaMain extends AbstractVerticle {

    private static final Logger LOG = LoggerFactory.getLogger(KetaMain.class);

    private final KetaConfig config;
    private final GrpcProxy<byte[], byte[]> proxy;
    private final KetaLeaderElector elector;
    private final URI listener;

    public KetaMain(KetaConfig config, GrpcProxy<byte[], byte[]> proxy, KetaLeaderElector elector)
        throws URISyntaxException {
        this.config = config;
        this.proxy = proxy;
        this.elector = elector;
        this.listener = elector.getListeners().isEmpty()
            ? new URI("http://0.0.0.0:2379")
            : elector.getListeners().get(0);
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        VertxServerBuilder serverBuilder = VertxServerBuilder
            .forAddress(vertx,
                this.context.config().getString("listen-address", listener.getHost()),
                this.context.config().getInteger("listen-port", listener.getPort()));

        List<ServerServiceDefinition> services = Arrays.asList(
            new AuthService(elector).bindService(),
            new KVService(elector).bindService(),
            new LeaseService(elector).bindService()
        );

        NettyServerBuilder nettyBuilder = serverBuilder.nettyBuilder()
            .permitKeepAliveWithoutCalls(true)
            .permitKeepAliveTime(5, TimeUnit.SECONDS)
            // may help with "java.net.BindException: address already in use"
            // see https://issues.apache.org/jira/browse/RATIS-606
            .withChildOption(ChannelOption.SO_REUSEADDR, true)
            .withChildOption(ChannelOption.TCP_NODELAY, true)
            .addService(new ClusterService(elector))
            .addService(new MaintenanceService(elector))
            .addService(new WatchService(elector))  // WatchService can go to any node
            .fallbackHandlerRegistry(new GrpcProxy.Registry(proxy, services))
            .intercept(new AuthServerInterceptor());

        if (isTls()) {
            nettyBuilder.sslContext(new SslFactory(config, true).sslContext());
        }

        VertxServer server = serverBuilder.build();

        server.start(ar -> {
            if (ar.succeeded()) {
                LOG.info("Server started, listening on " + listener.getPort());
                LOG.info("Keta is at your service...");
                startPromise.complete();
            } else {
                LOG.info("Could not start server " + ar.cause().getLocalizedMessage());
                startPromise.fail(ar.cause());
                LOG.error("Server died unexpectedly: ", ar.cause());
                System.exit(1);
            }
        });
    }

    private boolean isTls() {
        return listener.getScheme().equalsIgnoreCase("https");
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
            GrpcProxy<byte[], byte[]> proxy = new GrpcProxy<>(config, null);
            LOG.info("Starting leader election...");
            KetaLeaderElector elector = new KetaLeaderElector(config, engine, proxy);
            engine.init(elector, new VertxNotifier(vertx.eventBus()));
            elector.init();
            boolean isLeader = elector.isLeader();
            LOG.info("Leader: {}, starting server...", isLeader);
            vertx.deployVerticle(new KetaMain(config, proxy, elector));
        } catch (Exception e) {
            LOG.error("Server died unexpectedly: ", e);
            System.exit(1);
        }
    }
}

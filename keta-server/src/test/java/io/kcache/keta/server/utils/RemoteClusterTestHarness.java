/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.kcache.keta.server.utils;

import com.google.common.io.Files;
import io.kcache.keta.KetaConfig;
import io.kcache.keta.KetaEngine;
import io.kcache.keta.notifier.KetaNotifier;
import io.kcache.keta.server.KetaMain;
import io.kcache.keta.server.grpc.proxy.GrpcProxy;
import io.kcache.keta.server.leader.KetaLeaderElector;
import io.kcache.keta.utils.ClusterTestHarness;
import io.vertx.core.Vertx;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Collections;
import java.util.Properties;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test harness to run against a real, local Kafka cluster. This is essentially
 * Kafka's ZookeeperTestHarness and KafkaServerTestHarness traits combined.
 */
public abstract class RemoteClusterTestHarness extends ClusterTestHarness {

    private static final Logger LOG = LoggerFactory.getLogger(RemoteClusterTestHarness.class);

    public static final String ENDPOINTS = "http://127.0.0.1:2379";


    private GrpcProxy<byte[], byte[]> proxy;
    private KetaLeaderElector elector;

    protected File tempDir;
    protected Properties props;
    protected Integer serverPort;

    public RemoteClusterTestHarness() {
        super();
    }

    public RemoteClusterTestHarness(int numBrokers) {
        super(numBrokers);
    }

    public KetaMain createKeta() throws Exception {
        proxy = mock(GrpcProxy.class);
        elector = mock(KetaLeaderElector.class);
        when(elector.isLeader()).thenReturn(true);
        when(elector.getListeners()).thenReturn(Collections.emptyList());
        return new KetaMain(proxy, elector);
    }

    @BeforeEach
    public void setUp(Vertx vertx) throws Exception {
        super.setUp();
        if (tempDir == null) {
            tempDir = Files.createTempDir();
        }
        props = new Properties();
        setUpServer(vertx);
    }

    private void setUpServer(Vertx vertx) {
        try {
            serverPort = choosePort();
            injectKetaProperties(props);

            KetaConfig config = new KetaConfig(props);
            KetaEngine engine = KetaEngine.getInstance();
            engine.configure(config);
            engine.init(elector, new KetaNotifier(vertx.eventBus()));
        } catch (Exception e) {
            LOG.error("Server died unexpectedly: ", e);
            System.exit(1);
        }
    }

    protected void injectKetaProperties(Properties props) {
        props.put(KetaConfig.LISTENERS_CONFIG, "http://0.0.0.0:" + serverPort);
        props.put(KetaConfig.KAFKACACHE_BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(KetaConfig.KAFKACACHE_DATA_DIR_CONFIG, tempDir.getAbsolutePath());
    }

    /**
     * Choose a number of random available ports
     */
    public static int[] choosePorts(int count) {
        try {
            ServerSocket[] sockets = new ServerSocket[count];
            int[] ports = new int[count];
            for (int i = 0; i < count; i++) {
                sockets[i] = new ServerSocket(0, 0, InetAddress.getByName("0.0.0.0"));
                ports[i] = sockets[i].getLocalPort();
            }
            for (int i = 0; i < count; i++) {
                sockets[i].close();
            }
            return ports;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Choose an available port
     */
    public static int choosePort() {
        return choosePorts(1)[0];
    }

    @AfterEach
    public void tearDown() throws Exception {
        try {
            KetaEngine.closeInstance();
            FileUtils.deleteDirectory(tempDir);
        } catch (Exception e) {
            LOG.warn("Exception during tearDown", e);
        }
        super.tearDown();
    }
}

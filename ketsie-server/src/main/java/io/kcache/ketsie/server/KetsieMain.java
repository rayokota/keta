/*
 * Copyright 2015 The gRPC Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.kcache.ketsie.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.kcache.ketsie.KetsieConfig;
import io.kcache.ketsie.KetsieEngine;
import io.kcache.ketsie.server.kv.KVImpl;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Server that manages startup/shutdown of a {@code KVImpl} server.
 */
public class KetsieMain {
    private static final Logger logger = Logger.getLogger(KetsieMain.class.getName());

    private Server server;

    public void start(KetsieConfig config) throws IOException {
        KetsieEngine engine = KetsieEngine.getInstance();
        engine.configure(config);
        engine.init();
        /* The port on which the server should run */
        int port = 50051;
        server = ServerBuilder.forPort(port)
            .addService(new KVImpl().bindService())
            .build()
            .start();
        logger.info("Server started, listening on " + port);
        logger.info("Ketsie is at your service...");
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                try {
                    KetsieMain.this.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
                System.err.println("*** server shut down");
            }
        });
    }

    public void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    /**
     * Main launches the server from the command line.
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        final KetsieMain server = new KetsieMain();
        final KetsieConfig config = new KetsieConfig(args[0]);
        server.start(config);
        server.blockUntilShutdown();
    }
}

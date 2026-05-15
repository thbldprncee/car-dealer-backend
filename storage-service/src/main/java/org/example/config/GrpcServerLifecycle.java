package org.example.config;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.extern.slf4j.Slf4j;
import org.example.config.properties.GrpcServerProperties;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class GrpcServerLifecycle implements SmartLifecycle {

    private final int port;
    private final List<BindableService> services;
    private Server server;
    private boolean running;

    public GrpcServerLifecycle(
            GrpcServerProperties properties,
            List<BindableService> services
    ) {
        this.port = properties.port();
        this.services = services;
    }

    @Override
    public void start() {
        if (running) {
            return;
        }

        try {
            ServerBuilder<?> serverBuilder = ServerBuilder.forPort(port);
            services.forEach(serverBuilder::addService);
            server = serverBuilder.build().start();
            running = true;
            log.info("gRPC server started on port {} with {} service(s)", port, services.size());
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to start gRPC server on port " + port, exception);
        }
    }

    @Override
    public void stop() {
        if (server == null) {
            running = false;
            return;
        }

        try {
            server.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            server.shutdownNow();
        } finally {
            running = false;
            log.info("gRPC server stopped");
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}

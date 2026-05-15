package org.example.config;

import io.grpc.BindableService;
import io.grpc.health.v1.HealthCheckResponse.ServingStatus;
import io.grpc.protobuf.services.HealthStatusManager;
import io.grpc.protobuf.services.ProtoReflectionService;
import org.example.grpc.CarInventoryServiceGrpc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcHealthConfig {

    @Bean
    public HealthStatusManager healthStatusManager() {
        HealthStatusManager healthStatusManager = new HealthStatusManager();
        healthStatusManager.setStatus("", ServingStatus.SERVING);
        healthStatusManager.setStatus(CarInventoryServiceGrpc.SERVICE_NAME, ServingStatus.SERVING);
        return healthStatusManager;
    }

    @Bean
    public BindableService grpcHealthService(HealthStatusManager healthStatusManager) {
        return healthStatusManager.getHealthService();
    }

    @Bean
    public BindableService grpcReflectionService() {
        return ProtoReflectionService.newInstance();
    }
}

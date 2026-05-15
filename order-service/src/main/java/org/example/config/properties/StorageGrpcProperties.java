package org.example.config.properties;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.storage-grpc")
public record StorageGrpcProperties(
        @NotBlank String host,
        @Min(1) @Max(65535) int port,
        @Positive long timeoutMs
) {
}

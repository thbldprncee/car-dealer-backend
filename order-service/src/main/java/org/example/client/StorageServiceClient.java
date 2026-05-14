package org.example.client;

import lombok.RequiredArgsConstructor;
import org.example.domain.enums.CarColor;
import org.example.domain.exceptions.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StorageServiceClient {

    private final RestClient.Builder restClientBuilder;

    @Value("${app.storage-service.base-url:http://localhost:18081}")
    private String storageServiceBaseUrl;

    public CarConfigurationSnapshot getConfiguration(UUID configurationId) {
        try {
            return restClientBuilder.build()
                    .get()
                    .uri(storageServiceBaseUrl + "/api/configurations/{id}", configurationId)
                    .headers(headers -> bearerToken().ifPresent(headers::setBearerAuth))
                    .retrieve()
                    .body(CarConfigurationSnapshot.class);
        } catch (HttpClientErrorException.NotFound exception) {
            throw new EntityNotFoundException("CarConfiguration", configurationId);
        }
    }

    public CarSnapshot getCar(UUID carId) {
        try {
            return restClientBuilder.build()
                    .get()
                    .uri(storageServiceBaseUrl + "/api/cars/{id}", carId)
                    .headers(headers -> bearerToken().ifPresent(headers::setBearerAuth))
                    .retrieve()
                    .body(CarSnapshot.class);
        } catch (HttpClientErrorException.NotFound exception) {
            throw new EntityNotFoundException("Car", carId);
        }
    }

    private java.util.Optional<String> bearerToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            return java.util.Optional.of(jwtAuthentication.getToken().getTokenValue());
        }

        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return java.util.Optional.of(jwt.getTokenValue());
        }

        return java.util.Optional.empty();
    }

    public record CarConfigurationSnapshot(
            UUID id,
            UUID carModelId,
            String carModelDisplayName,
            BigDecimal totalPrice
    ) {
    }

    public record CarSnapshot(
            UUID id,
            UUID carModelId,
            String carModelDisplayName,
            String vin,
            CarColor color,
            BigDecimal price
    ) {

        public String displayName() {
            return carModelDisplayName + " / " + vin;
        }
    }
}

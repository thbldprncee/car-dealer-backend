package io.github.thbldprncee.cardealer.order;

import io.github.thbldprncee.cardealer.order.config.security.KeycloakJwtAuthenticationConverter;
import io.github.thbldprncee.cardealer.order.config.security.SecurityConfig;
import io.github.thbldprncee.cardealer.order.domain.exceptions.EntityNotFoundException;
import io.github.thbldprncee.cardealer.order.domain.exceptions.GrpcServiceUnavailableException;
import io.github.thbldprncee.cardealer.order.dto.response.AvailableCarResponse;
import io.github.thbldprncee.cardealer.order.presentation.AvailableCarController;
import io.github.thbldprncee.cardealer.order.presentation.handler.GlobalExceptionHandler;
import io.github.thbldprncee.cardealer.order.service.AvailableCarService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AvailableCarController.class)
@ContextConfiguration(classes = {
        AvailableCarController.class,
        SecurityConfig.class,
        KeycloakJwtAuthenticationConverter.class,
        GlobalExceptionHandler.class
})
@Import({SecurityConfig.class, KeycloakJwtAuthenticationConverter.class, GlobalExceptionHandler.class})
class AvailableCarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AvailableCarService availableCarService;

    @Test
    void shouldReturnAvailableCarsFromGrpcClient() throws Exception {
        UUID carId = UUID.randomUUID();
        when(availableCarService.getAvailableCars()).thenReturn(List.of(carResponse(carId)));

        mockMvc.perform(get("/api/v1/cars")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENT"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(carId.toString()))
                .andExpect(jsonPath("$[0].carModelDisplayName").value("BMW 320i"));
    }

    @Test
    void shouldReturnAvailableCarByIdFromGrpcClient() throws Exception {
        UUID carId = UUID.randomUUID();
        when(availableCarService.getAvailableCarById(carId)).thenReturn(carResponse(carId));

        mockMvc.perform(get("/api/v1/cars/{id}", carId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SALES_MANAGER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(carId.toString()))
                .andExpect(jsonPath("$.vin").value("VIN-BMW-320I-0001"));
    }

    @Test
    void shouldReturnServiceUnavailableWhenGrpcStorageServiceIsUnavailable() throws Exception {
        when(availableCarService.getAvailableCars())
                .thenThrow(new GrpcServiceUnavailableException("StorageService is unavailable"));

        mockMvc.perform(get("/api/v1/cars")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503));
    }

    @Test
    void shouldReturnNotFoundWhenGrpcStorageServiceReturnsNotFound() throws Exception {
        UUID carId = UUID.randomUUID();
        when(availableCarService.getAvailableCarById(carId)).thenThrow(new EntityNotFoundException("Car", carId));

        mockMvc.perform(get("/api/v1/cars/{id}", carId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENT"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldRejectUnauthenticatedUser() throws Exception {
        mockMvc.perform(get("/api/v1/cars"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectWarehouseAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/cars")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_WAREHOUSE_ADMIN"))))
                .andExpect(status().isForbidden());
    }

    private AvailableCarResponse carResponse(UUID carId) {
        return new AvailableCarResponse(
                carId,
                UUID.randomUUID(),
                "BMW 320i",
                "VIN-BMW-320I-0001",
                "BLACK",
                new BigDecimal("3500000.00"),
                true,
                true
        );
    }
}

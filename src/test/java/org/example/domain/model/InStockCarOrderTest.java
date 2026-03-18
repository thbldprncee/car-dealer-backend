package org.example.domain.model;

import org.example.domain.enums.BodyType;
import org.example.domain.enums.CarColor;
import org.example.domain.enums.ComponentType;
import org.example.domain.enums.DriveType;
import org.example.domain.enums.FuelType;
import org.example.domain.enums.InStockOrderStatus;
import org.example.domain.enums.TransmissionType;
import org.example.domain.exceptions.DomainValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InStockCarOrderTest {

    private Client client;
    private SalesManager manager;
    private Car car;

    @BeforeEach
    void setUp() {
        client = new Client(UUID.randomUUID(), "Ivan", "Petrov", "ivan@test.com");
        manager = new SalesManager(UUID.randomUUID(), "Anna", "Smirnova", "anna@test.com");

        UUID modelId = UUID.randomUUID();

        ComponentOption wheels = new ComponentOption(UUID.randomUUID(), ComponentType.WHEELS, "Wheels", BigDecimal.ZERO, Set.of(modelId));
        ComponentOption transmission = new ComponentOption(UUID.randomUUID(), ComponentType.TRANSMISSION, "Transmission", BigDecimal.ZERO, Set.of(modelId));
        ComponentOption steering = new ComponentOption(UUID.randomUUID(), ComponentType.STEERING_WHEEL, "Steering", BigDecimal.ZERO, Set.of(modelId));
        ComponentOption interior = new ComponentOption(UUID.randomUUID(), ComponentType.INTERIOR, "Interior", BigDecimal.ZERO, Set.of(modelId));

        Map<ComponentType, ComponentOption> baseComponents = new EnumMap<>(ComponentType.class);
        baseComponents.put(ComponentType.WHEELS, wheels);
        baseComponents.put(ComponentType.TRANSMISSION, transmission);
        baseComponents.put(ComponentType.STEERING_WHEEL, steering);
        baseComponents.put(ComponentType.INTERIOR, interior);

        CarModel model = new CarModel(
                modelId,
                "BMW",
                "320i",
                BodyType.SEDAN,
                FuelType.PETROL,
                184,
                2.0,
                TransmissionType.AUTOMATIC,
                DriveType.REAR_WHEEL,
                new BigDecimal("3500000"),
                Set.of(ComponentType.WHEELS, ComponentType.TRANSMISSION, ComponentType.STEERING_WHEEL, ComponentType.INTERIOR),
                baseComponents
        );

        car = new Car(
                UUID.randomUUID(),
                model,
                "VIN-001",
                CarColor.BLACK,
                new BigDecimal("3650000"),
                true,
                true
        );
    }

    @Test
    void shouldGoThroughHappyStatusFlow() {
        InStockCarOrder order = new InStockCarOrder(
                UUID.randomUUID(),
                client,
                manager,
                car,
                LocalDateTime.now(),
                InStockOrderStatus.CREATED
        );

        order.approveByManager();
        assertEquals(InStockOrderStatus.APPROVED_BY_MANAGER, order.getStatus());

        order.markWaitingForPayment();
        assertEquals(InStockOrderStatus.WAITING_FOR_PAYMENT, order.getStatus());

        order.markAsPaid();
        assertEquals(InStockOrderStatus.PAID, order.getStatus());

        order.markReadyForDelivery();
        assertEquals(InStockOrderStatus.READY_FOR_DELIVERY, order.getStatus());

        order.complete();
        assertEquals(InStockOrderStatus.COMPLETED, order.getStatus());
    }

    @Test
    void shouldThrowOnInvalidTransition() {
        InStockCarOrder order = new InStockCarOrder(
                UUID.randomUUID(),
                client,
                manager,
                car,
                LocalDateTime.now(),
                InStockOrderStatus.CREATED
        );

        assertThrows(DomainValidationException.class, order::markAsPaid);
    }

    @Test
    void shouldCancelOrder() {
        InStockCarOrder order = new InStockCarOrder(
                UUID.randomUUID(),
                client,
                manager,
                car,
                LocalDateTime.now(),
                InStockOrderStatus.CREATED
        );

        order.cancel();
        assertEquals(InStockOrderStatus.CANCELED, order.getStatus());
    }
}
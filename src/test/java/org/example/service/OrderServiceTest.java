package org.example.service;

import org.example.domain.enums.BodyType;
import org.example.domain.enums.CarColor;
import org.example.domain.enums.ComponentType;
import org.example.domain.enums.CustomOrderStatus;
import org.example.domain.enums.DriveType;
import org.example.domain.enums.FuelType;
import org.example.domain.enums.InStockOrderStatus;
import org.example.domain.enums.TransmissionType;
import org.example.domain.exceptions.DomainValidationException;
import org.example.domain.exceptions.EntityNotFoundException;
import org.example.domain.model.Car;
import org.example.domain.model.CarModel;
import org.example.domain.model.Client;
import org.example.domain.model.SalesManager;
import org.example.dto.request.CreateCustomCarOrderRequest;
import org.example.dto.request.CreateInStockCarOrderRequest;
import org.example.dto.response.CustomCarOrderResponse;
import org.example.dto.response.InStockCarOrderResponse;
import org.example.repository.CarConfigurationRepository;
import org.example.repository.CarRepository;
import org.example.repository.CustomCarOrderRepository;
import org.example.repository.InStockCarOrderRepository;
import org.example.repository.UserRepository;
import org.example.repository.inmemory.InMemoryCarConfigurationRepository;
import org.example.repository.inmemory.InMemoryCarRepository;
import org.example.repository.inmemory.InMemoryCustomCarOrderRepository;
import org.example.repository.inmemory.InMemoryInStockCarOrderRepository;
import org.example.repository.inmemory.InMemoryUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrderServiceTest {

    private OrderService orderService;

    private UserRepository userRepository;
    private CarRepository carRepository;
    private CarConfigurationRepository carConfigurationRepository;
    private InStockCarOrderRepository inStockCarOrderRepository;
    private CustomCarOrderRepository customCarOrderRepository;

    private Client client;
    private SalesManager salesManager;
    private CarModel carModel;
    private Car availableCar;
    private org.example.domain.model.CarConfiguration configuration;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        carRepository = new InMemoryCarRepository();
        carConfigurationRepository = new InMemoryCarConfigurationRepository();
        inStockCarOrderRepository = new InMemoryInStockCarOrderRepository();
        customCarOrderRepository = new InMemoryCustomCarOrderRepository();

        orderService = new OrderService(
                userRepository,
                carRepository,
                carConfigurationRepository,
                inStockCarOrderRepository,
                customCarOrderRepository
        );

        client = new Client(
                UUID.randomUUID(),
                "Ivan",
                "Petrov",
                "ivan.petrov@example.com"
        );

        salesManager = new SalesManager(
                UUID.randomUUID(),
                "Anna",
                "Smirnova",
                "anna.smirnova@example.com"
        );

        userRepository.save(client);
        userRepository.save(salesManager);

        UUID carModelId = UUID.randomUUID();

        org.example.domain.model.ComponentOption wheels = new org.example.domain.model.ComponentOption(
                UUID.randomUUID(),
                ComponentType.WHEELS,
                "17 Standard",
                BigDecimal.ZERO,
                java.util.Set.of(carModelId)
        );

        org.example.domain.model.ComponentOption transmission = new org.example.domain.model.ComponentOption(
                UUID.randomUUID(),
                ComponentType.TRANSMISSION,
                "Automatic 8AT",
                BigDecimal.ZERO,
                java.util.Set.of(carModelId)
        );

        org.example.domain.model.ComponentOption steering = new org.example.domain.model.ComponentOption(
                UUID.randomUUID(),
                ComponentType.STEERING_WHEEL,
                "Sport Leather Standard",
                BigDecimal.ZERO,
                java.util.Set.of(carModelId)
        );

        org.example.domain.model.ComponentOption interior = new org.example.domain.model.ComponentOption(
                UUID.randomUUID(),
                ComponentType.INTERIOR,
                "Fabric Graphite",
                BigDecimal.ZERO,
                java.util.Set.of(carModelId)
        );

        Map<ComponentType, org.example.domain.model.ComponentOption> baseComponents = new EnumMap<>(ComponentType.class);
        baseComponents.put(ComponentType.WHEELS, wheels);
        baseComponents.put(ComponentType.TRANSMISSION, transmission);
        baseComponents.put(ComponentType.STEERING_WHEEL, steering);
        baseComponents.put(ComponentType.INTERIOR, interior);

        carModel = new CarModel(
                carModelId,
                "BMW",
                "320i",
                BodyType.SEDAN,
                FuelType.PETROL,
                184,
                2.0,
                TransmissionType.AUTOMATIC,
                DriveType.REAR_WHEEL,
                new BigDecimal("3500000"),
                java.util.Set.of(
                        ComponentType.WHEELS,
                        ComponentType.TRANSMISSION,
                        ComponentType.STEERING_WHEEL,
                        ComponentType.INTERIOR
                ),
                baseComponents
        );

        availableCar = new Car(
                UUID.randomUUID(),
                carModel,
                "VIN-BMW-320I-0001",
                CarColor.BLACK,
                new BigDecimal("3650000"),
                true,
                true
        );

        configuration = new org.example.domain.model.CarConfiguration(
                UUID.randomUUID(),
                carModel,
                baseComponents
        );

        carRepository.save(availableCar);
        carConfigurationRepository.save(configuration);
    }

    @Test
    void shouldCreateInStockOrder() {
        CreateInStockCarOrderRequest request = new CreateInStockCarOrderRequest(
                client.getId(),
                availableCar.getId()
        );

        InStockCarOrderResponse response = orderService.createInStockOrder(request);

        assertNotNull(response);
        assertEquals(client.getId(), response.clientId());
        assertEquals(salesManager.getId(), response.salesManagerId());
        assertEquals(availableCar.getId(), response.carId());
        assertEquals(InStockOrderStatus.CREATED, response.status());
    }

    @Test
    void shouldMakeCarUnavailableAfterCreatingInStockOrder() {
        CreateInStockCarOrderRequest request = new CreateInStockCarOrderRequest(
                client.getId(),
                availableCar.getId()
        );

        orderService.createInStockOrder(request);

        Car updatedCar = carRepository.findById(availableCar.getId()).orElseThrow();
        assertFalse(updatedCar.isAvailable());
        assertFalse(updatedCar.isAvailableForTestDrive());
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenClientDoesNotExistForInStockOrder() {
        CreateInStockCarOrderRequest request = new CreateInStockCarOrderRequest(
                UUID.randomUUID(),
                availableCar.getId()
        );

        assertThrows(EntityNotFoundException.class,
                () -> orderService.createInStockOrder(request));
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenCarDoesNotExistForInStockOrder() {
        CreateInStockCarOrderRequest request = new CreateInStockCarOrderRequest(
                client.getId(),
                UUID.randomUUID()
        );

        assertThrows(EntityNotFoundException.class,
                () -> orderService.createInStockOrder(request));
    }

    @Test
    void shouldThrowDomainValidationExceptionWhenNoSalesManagerExists() {
        UserRepository emptyUserRepository = new InMemoryUserRepository();
        emptyUserRepository.save(client);

        OrderService orderServiceWithoutManager = new OrderService(
                emptyUserRepository,
                carRepository,
                carConfigurationRepository,
                inStockCarOrderRepository,
                customCarOrderRepository
        );

        CreateInStockCarOrderRequest request = new CreateInStockCarOrderRequest(
                client.getId(),
                availableCar.getId()
        );

        assertThrows(DomainValidationException.class,
                () -> orderServiceWithoutManager.createInStockOrder(request));
    }

    @Test
    void shouldCreateCustomOrder() {
        CreateCustomCarOrderRequest request = new CreateCustomCarOrderRequest(
                client.getId(),
                carModel.getId(),
                configuration.getId()
        );

        CustomCarOrderResponse response = orderService.createCustomOrder(request);

        assertNotNull(response);
        assertEquals(client.getId(), response.clientId());
        assertEquals(salesManager.getId(), response.salesManagerId());
        assertEquals(carModel.getId(), response.carModelId());
        assertEquals(configuration.getId(), response.carConfigurationId());
        assertEquals(CustomOrderStatus.CREATED, response.status());
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenConfigurationDoesNotExist() {
        CreateCustomCarOrderRequest request = new CreateCustomCarOrderRequest(
                client.getId(),
                carModel.getId(),
                UUID.randomUUID()
        );

        assertThrows(EntityNotFoundException.class,
                () -> orderService.createCustomOrder(request));
    }

    @Test
    void shouldThrowDomainValidationExceptionWhenConfigurationModelDoesNotMatchRequestedModel() {
        UUID anotherModelId = UUID.randomUUID();

        CreateCustomCarOrderRequest request = new CreateCustomCarOrderRequest(
                client.getId(),
                anotherModelId,
                configuration.getId()
        );

        assertThrows(DomainValidationException.class,
                () -> orderService.createCustomOrder(request));
    }

    @Test
    void shouldApproveAndCompleteInStockOrderFlow() {
        InStockCarOrderResponse created = orderService.createInStockOrder(
                new CreateInStockCarOrderRequest(client.getId(), availableCar.getId())
        );

        assertEquals(InStockOrderStatus.APPROVED_BY_MANAGER,
                orderService.approveInStockOrderByManager(created.id()).status());

        assertEquals(InStockOrderStatus.WAITING_FOR_PAYMENT,
                orderService.moveInStockOrderToWaitingForPayment(created.id()).status());

        assertEquals(InStockOrderStatus.PAID,
                orderService.payInStockOrder(created.id()).status());

        assertEquals(InStockOrderStatus.READY_FOR_DELIVERY,
                orderService.markInStockOrderReadyForDelivery(created.id()).status());

        assertEquals(InStockOrderStatus.COMPLETED,
                orderService.completeInStockOrder(created.id()).status());
    }

    @Test
    void shouldApproveAndCompleteCustomOrderFlow() {
        CustomCarOrderResponse created = orderService.createCustomOrder(
                new CreateCustomCarOrderRequest(client.getId(), carModel.getId(), configuration.getId())
        );

        assertEquals(CustomOrderStatus.APPROVED_BY_WAREHOUSE,
                orderService.approveCustomOrderByWarehouse(created.id()).status());

        assertEquals(CustomOrderStatus.WAITING_FOR_PAYMENT,
                orderService.moveCustomOrderToWaitingForPayment(created.id()).status());

        assertEquals(CustomOrderStatus.PAID,
                orderService.payCustomOrder(created.id()).status());

        assertEquals(CustomOrderStatus.WAITING_FOR_DELIVERY,
                orderService.moveCustomOrderToWaitingForDelivery(created.id()).status());

        assertEquals(CustomOrderStatus.READY_FOR_DELIVERY,
                orderService.markCustomOrderReadyForDelivery(created.id()).status());

        assertEquals(CustomOrderStatus.COMPLETED,
                orderService.completeCustomOrder(created.id()).status());
    }
}
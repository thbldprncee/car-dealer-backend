package org.example;

import org.example.domain.enums.BodyType;
import org.example.domain.enums.CarColor;
import org.example.domain.enums.ComponentType;
import org.example.domain.enums.DriveType;
import org.example.domain.enums.FuelType;
import org.example.domain.enums.TransmissionType;
import org.example.domain.model.Client;
import org.example.domain.model.SalesManager;
import org.example.dto.request.BuildCarConfigurationRequest;
import org.example.dto.request.CarFilterRequest;
import org.example.dto.request.CreateCarModelRequest;
import org.example.dto.request.CreateCarRequest;
import org.example.dto.request.CreateComponentOptionRequest;
import org.example.dto.request.CreateInStockCarOrderRequest;
import org.example.dto.request.CreateTestDriveRequest;
import org.example.dto.response.CarConfigurationResponse;
import org.example.dto.response.CarModelResponse;
import org.example.dto.response.CarResponse;
import org.example.dto.response.ComponentOptionResponse;
import org.example.dto.response.InStockCarOrderResponse;
import org.example.dto.response.TestDriveRequestResponse;
import org.example.presentation.CarController;
import org.example.presentation.InventoryController;
import org.example.presentation.OrderController;
import org.example.presentation.TestDriveController;
import org.example.repository.CarConfigurationRepository;
import org.example.repository.CarModelRepository;
import org.example.repository.CarRepository;
import org.example.repository.ComponentOptionRepository;
import org.example.repository.CustomCarOrderRepository;
import org.example.repository.InStockCarOrderRepository;
import org.example.repository.SparePartRepository;
import org.example.repository.TestDriveRequestRepository;
import org.example.repository.UserRepository;
import org.example.repository.inmemory.InMemoryCarConfigurationRepository;
import org.example.repository.inmemory.InMemoryCarModelRepository;
import org.example.repository.inmemory.InMemoryCarRepository;
import org.example.repository.inmemory.InMemoryComponentOptionRepository;
import org.example.repository.inmemory.InMemoryCustomCarOrderRepository;
import org.example.repository.inmemory.InMemoryInStockCarOrderRepository;
import org.example.repository.inmemory.InMemorySparePartRepository;
import org.example.repository.inmemory.InMemoryTestDriveRequestRepository;
import org.example.repository.inmemory.InMemoryUserRepository;
import org.example.service.CarService;
import org.example.service.ConfiguratorService;
import org.example.service.InventoryService;
import org.example.service.OrderService;
import org.example.service.TestDriveService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class App {

    public static void main(String[] args) {
        UserRepository userRepository = new InMemoryUserRepository();
        CarRepository carRepository = new InMemoryCarRepository();
        CarModelRepository carModelRepository = new InMemoryCarModelRepository();
        SparePartRepository sparePartRepository = new InMemorySparePartRepository();
        ComponentOptionRepository componentOptionRepository = new InMemoryComponentOptionRepository();
        CarConfigurationRepository carConfigurationRepository = new InMemoryCarConfigurationRepository();
        InStockCarOrderRepository inStockCarOrderRepository = new InMemoryInStockCarOrderRepository();
        CustomCarOrderRepository customCarOrderRepository = new InMemoryCustomCarOrderRepository();
        TestDriveRequestRepository testDriveRequestRepository = new InMemoryTestDriveRequestRepository();

        InventoryService inventoryService = new InventoryService(
                carRepository,
                carModelRepository,
                sparePartRepository,
                componentOptionRepository
        );
        CarService carService = new CarService(carRepository, carModelRepository);
        ConfiguratorService configuratorService = new ConfiguratorService(
                carModelRepository,
                componentOptionRepository,
                carConfigurationRepository
        );
        OrderService orderService = new OrderService(
                userRepository,
                carRepository,
                carConfigurationRepository,
                inStockCarOrderRepository,
                customCarOrderRepository
        );
        TestDriveService testDriveService = new TestDriveService(
                userRepository,
                carRepository,
                testDriveRequestRepository
        );

        InventoryController inventoryController = new InventoryController(inventoryService);
        CarController carController = new CarController(carService);
        OrderController orderController = new OrderController(orderService);
        TestDriveController testDriveController = new TestDriveController(testDriveService);

        Client client = new Client(
                UUID.randomUUID(),
                "Ivan",
                "Petrov",
                "ivan.petrov@example.com"
        );
        SalesManager manager = new SalesManager(
                UUID.randomUUID(),
                "Anna",
                "Smirnova",
                "anna.smirnova@example.com"
        );

        userRepository.save(client);
        userRepository.save(manager);

        UUID bmw320iId = UUID.randomUUID();
        UUID bmw330iId = UUID.randomUUID();

        ComponentOptionResponse wheels17 = inventoryController.createComponentOption(
                new CreateComponentOptionRequest(
                        ComponentType.WHEELS,
                        "17 Standard",
                        BigDecimal.ZERO,
                        Set.of(bmw320iId)
                )
        );

        ComponentOptionResponse transmission8at = inventoryController.createComponentOption(
                new CreateComponentOptionRequest(
                        ComponentType.TRANSMISSION,
                        "Automatic 8AT",
                        BigDecimal.ZERO,
                        Set.of(bmw320iId, bmw330iId)
                )
        );

        ComponentOptionResponse steeringStandard = inventoryController.createComponentOption(
                new CreateComponentOptionRequest(
                        ComponentType.STEERING_WHEEL,
                        "Sport Leather Standard",
                        BigDecimal.ZERO,
                        Set.of(bmw320iId, bmw330iId)
                )
        );

        ComponentOptionResponse interiorGraphite = inventoryController.createComponentOption(
                new CreateComponentOptionRequest(
                        ComponentType.INTERIOR,
                        "Fabric Graphite",
                        BigDecimal.ZERO,
                        Set.of(bmw320iId)
                )
        );

        ComponentOptionResponse wheels19 = inventoryController.createComponentOption(
                new CreateComponentOptionRequest(
                        ComponentType.WHEELS,
                        "19 M-Sport",
                        new BigDecimal("95000"),
                        Set.of(bmw320iId, bmw330iId)
                )
        );

        ComponentOptionResponse steeringHeated = inventoryController.createComponentOption(
                new CreateComponentOptionRequest(
                        ComponentType.STEERING_WHEEL,
                        "M-Sport Heated",
                        new BigDecimal("25000"),
                        Set.of(bmw320iId, bmw330iId)
                )
        );

        ComponentOptionResponse interiorDakota = inventoryController.createComponentOption(
                new CreateComponentOptionRequest(
                        ComponentType.INTERIOR,
                        "Dakota Leather",
                        new BigDecimal("110000"),
                        Set.of(bmw320iId, bmw330iId)
                )
        );

        CarModelResponse bmw320i = inventoryController.createCarModel(
                new CreateCarModelRequest(
                        bmw320iId,
                        "BMW",
                        "320i",
                        BodyType.SEDAN,
                        FuelType.PETROL,
                        184,
                        2.0,
                        TransmissionType.AUTOMATIC,
                        DriveType.REAR_WHEEL,
                        new BigDecimal("3500000"),
                        Set.of(
                                ComponentType.WHEELS,
                                ComponentType.TRANSMISSION,
                                ComponentType.STEERING_WHEEL,
                                ComponentType.INTERIOR
                        ),
                        Map.of(
                                ComponentType.WHEELS, wheels17.id(),
                                ComponentType.TRANSMISSION, transmission8at.id(),
                                ComponentType.STEERING_WHEEL, steeringStandard.id(),
                                ComponentType.INTERIOR, interiorGraphite.id()
                        )
                )
        );

        CarResponse car = inventoryController.createCar(
                new CreateCarRequest(
                        bmw320i.id(),
                        "VIN-BMW-320I-0001",
                        CarColor.BLACK,
                        new BigDecimal("3650000"),
                        true,
                        true
                )
        );

        printSection("AVAILABLE CARS");
        carController.getAvailableCars().forEach(System.out::println);

        printSection("FILTER BMW 320i");
        carController.filterCars(
                new CarFilterRequest(
                        null,
                        null,
                        "BMW",
                        "320i",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                )
        ).forEach(System.out::println);

        printSection("BUILD CONFIGURATION");
        CarConfigurationResponse configuration = configuratorService.buildConfiguration(
                new BuildCarConfigurationRequest(
                        bmw320i.id(),
                        Map.of(
                                ComponentType.WHEELS, wheels19.id(),
                                ComponentType.TRANSMISSION, transmission8at.id(),
                                ComponentType.STEERING_WHEEL, steeringHeated.id(),
                                ComponentType.INTERIOR, interiorDakota.id()
                        )
                )
        );
        System.out.println(configuration);

        printSection("CREATE IN-STOCK ORDER");
        InStockCarOrderResponse order = orderController.createInStockOrder(
                new CreateInStockCarOrderRequest(
                        client.getId(),
                        car.id()
                )
        );
        System.out.println(order);

        printSection("CREATE TEST DRIVE REQUEST");
        CarResponse secondCar = inventoryController.createCar(
                new CreateCarRequest(
                        bmw320i.id(),
                        "VIN-BMW-320I-0002",
                        CarColor.WHITE,
                        new BigDecimal("3620000"),
                        true,
                        true
                )
        );

        TestDriveRequestResponse testDriveRequest = testDriveController.createTestDriveRequest(
                new CreateTestDriveRequest(
                        client.getId(),
                        secondCar.id(),
                        LocalDateTime.now().plusDays(1)
                )
        );
        System.out.println(testDriveRequest);
    }

    private static void printSection(String title) {
        System.out.println();
        System.out.println("=== " + title + " ===");
    }
}
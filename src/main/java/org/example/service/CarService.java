package org.example.service;

import org.example.domain.exceptions.DomainValidationException;
import org.example.domain.exceptions.EntityNotFoundException;
import org.example.domain.model.Car;
import org.example.domain.model.CarModel;
import org.example.dto.request.CarFilterRequest;
import org.example.dto.request.CreateCarRequest;
import org.example.dto.request.UpdateCarRequest;
import org.example.dto.response.CarResponse;
import org.example.mappers.CarMapper;
import org.example.repository.CarModelRepository;
import org.example.repository.CarRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CarService {

    private final CarRepository carRepository;
    private final CarModelRepository carModelRepository;
    private final CarMapper carMapper;

    public CarService(CarRepository carRepository, CarModelRepository carModelRepository) {
        this.carRepository = carRepository;
        this.carModelRepository = carModelRepository;
        this.carMapper = CarMapper.INSTANCE;
    }

    public CarResponse createCar(CreateCarRequest request) {
        validateCreateRequest(request);

        carRepository.findByVin(request.vin())
                .ifPresent(existingCar -> {
                    throw new DomainValidationException(
                            "Car with vin=%s already exists".formatted(request.vin())
                    );
                });

        CarModel carModel = carModelRepository.findById(request.carModelId())
                .orElseThrow(() -> new EntityNotFoundException("CarModel", request.carModelId()));

        Car car = new Car(
                UUID.randomUUID(),
                carModel,
                request.vin(),
                request.color(),
                request.price(),
                request.available(),
                request.availableForTestDrive()
        );

        return carMapper.toResponse(carRepository.save(car));
    }

    public CarResponse getCarById(UUID id) {
        return carRepository.findById(id)
                .map(carMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Car", id));
    }

    public List<CarResponse> getAllCars() {
        return carRepository.findAll().stream()
                .map(carMapper::toResponse)
                .toList();
    }

    public List<CarResponse> getAvailableCars() {
        return carRepository.findAvailableCars().stream()
                .map(carMapper::toResponse)
                .toList();
    }

    public List<CarResponse> getCarsAvailableForTestDrive() {
        return carRepository.findCarsAvailableForTestDrive().stream()
                .map(carMapper::toResponse)
                .toList();
    }

    public List<CarResponse> filterCars(CarFilterRequest filter) {
        return carRepository.findAvailableCars().stream()
                .filter(car -> matchesMinPrice(car, filter))
                .filter(car -> matchesMaxPrice(car, filter))
                .filter(car -> matchesBrand(car, filter))
                .filter(car -> matchesModelName(car, filter))
                .filter(car -> matchesBodyType(car, filter))
                .filter(car -> matchesFuelType(car, filter))
                .filter(car -> matchesMinEnginePower(car, filter))
                .filter(car -> matchesMaxEnginePower(car, filter))
                .filter(car -> matchesMinEngineVolume(car, filter))
                .filter(car -> matchesMaxEngineVolume(car, filter))
                .filter(car -> matchesTransmissionType(car, filter))
                .filter(car -> matchesDriveType(car, filter))
                .filter(car -> matchesColor(car, filter))
                .map(carMapper::toResponse)
                .toList();
    }

    public CarResponse updateCar(UUID id, UpdateCarRequest request) {
        validateUpdateRequest(request);

        Car car = carRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Car", id));

        Optional.ofNullable(request.color())
                .ifPresent(car::updateColor);

        Optional.ofNullable(request.price())
                .ifPresent(car::updatePrice);

        Optional.ofNullable(request.available())
                .ifPresent(available -> {
                    if (available) {
                        car.markAsAvailable();
                    } else {
                        car.markAsUnavailable();
                    }
                });

        Optional.ofNullable(request.availableForTestDrive())
                .ifPresent(availableForTestDrive -> {
                    if (availableForTestDrive) {
                        car.enableTestDrive();
                    } else {
                        car.disableTestDrive();
                    }
                });

        return carMapper.toResponse(carRepository.save(car));
    }

    public void deleteCar(UUID id) {
        if (!carRepository.existsById(id)) {
            throw new EntityNotFoundException("Car", id);
        }
        carRepository.deleteById(id);
    }

    private void validateCreateRequest(CreateCarRequest request) {
        if (request == null) {
            throw new DomainValidationException("Create car request must not be null");
        }
    }

    private void validateUpdateRequest(UpdateCarRequest request) {
        if (request == null) {
            throw new DomainValidationException("Update car request must not be null");
        }
    }

    private boolean matchesMinPrice(Car car, CarFilterRequest filter) {
        return Optional.ofNullable(filter)
                .map(CarFilterRequest::minPrice)
                .map(minPrice -> car.getPrice().compareTo(minPrice) >= 0)
                .orElse(true);
    }

    private boolean matchesMaxPrice(Car car, CarFilterRequest filter) {
        return Optional.ofNullable(filter)
                .map(CarFilterRequest::maxPrice)
                .map(maxPrice -> car.getPrice().compareTo(maxPrice) <= 0)
                .orElse(true);
    }

    private boolean matchesBrand(Car car, CarFilterRequest filter) {
        return Optional.ofNullable(filter)
                .map(CarFilterRequest::brand)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(brand -> car.getCarModel().getBrand().equalsIgnoreCase(brand))
                .orElse(true);
    }

    private boolean matchesModelName(Car car, CarFilterRequest filter) {
        if (filter == null) {
            return true;
        }

        String brand = Optional.ofNullable(filter.brand())
                .map(String::trim)
                .orElse("");

        String modelName = Optional.ofNullable(filter.modelName())
                .map(String::trim)
                .orElse("");

        if (brand.isBlank() || modelName.isBlank()) {
            return true;
        }

        return car.getCarModel().getModelName().equalsIgnoreCase(modelName);
    }

    private boolean matchesBodyType(Car car, CarFilterRequest filter) {
        return Optional.ofNullable(filter)
                .map(CarFilterRequest::bodyType)
                .map(bodyType -> car.getCarModel().getBodyType() == bodyType)
                .orElse(true);
    }

    private boolean matchesFuelType(Car car, CarFilterRequest filter) {
        return Optional.ofNullable(filter)
                .map(CarFilterRequest::fuelType)
                .map(fuelType -> car.getCarModel().getFuelType() == fuelType)
                .orElse(true);
    }

    private boolean matchesMinEnginePower(Car car, CarFilterRequest filter) {
        return Optional.ofNullable(filter)
                .map(CarFilterRequest::minEnginePower)
                .map(minPower -> car.getCarModel().getEnginePower() >= minPower)
                .orElse(true);
    }

    private boolean matchesMaxEnginePower(Car car, CarFilterRequest filter) {
        return Optional.ofNullable(filter)
                .map(CarFilterRequest::maxEnginePower)
                .map(maxPower -> car.getCarModel().getEnginePower() <= maxPower)
                .orElse(true);
    }

    private boolean matchesMinEngineVolume(Car car, CarFilterRequest filter) {
        return Optional.ofNullable(filter)
                .map(CarFilterRequest::minEngineVolume)
                .map(minVolume -> car.getCarModel().getEngineVolume() >= minVolume)
                .orElse(true);
    }

    private boolean matchesMaxEngineVolume(Car car, CarFilterRequest filter) {
        return Optional.ofNullable(filter)
                .map(CarFilterRequest::maxEngineVolume)
                .map(maxVolume -> car.getCarModel().getEngineVolume() <= maxVolume)
                .orElse(true);
    }

    private boolean matchesTransmissionType(Car car, CarFilterRequest filter) {
        return Optional.ofNullable(filter)
                .map(CarFilterRequest::transmissionType)
                .map(transmissionType -> car.getCarModel().getTransmissionType() == transmissionType)
                .orElse(true);
    }

    private boolean matchesDriveType(Car car, CarFilterRequest filter) {
        return Optional.ofNullable(filter)
                .map(CarFilterRequest::driveType)
                .map(driveType -> car.getCarModel().getDriveType() == driveType)
                .orElse(true);
    }

    private boolean matchesColor(Car car, CarFilterRequest filter) {
        return Optional.ofNullable(filter)
                .map(CarFilterRequest::color)
                .map(color -> car.getColor() == color)
                .orElse(true);
    }
}
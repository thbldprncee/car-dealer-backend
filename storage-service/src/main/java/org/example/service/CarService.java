package org.example.service;

import lombok.RequiredArgsConstructor;
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
import org.example.repository.specification.CarSpecification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CarService {

    private final CarRepository carRepository;
    private final CarModelRepository carModelRepository;

    private final CarMapper carMapper;

    @Transactional
    public CarResponse createCar(CreateCarRequest request) {
        validateCreateRequest(request);

        carRepository.findByVinAndRemovedFalse(request.vin())
                .ifPresent(existingCar -> {
                    throw new DomainValidationException(
                            "Car with vin=%s already exists".formatted(request.vin())
                    );
                });

        CarModel carModel = carModelRepository.findByIdAndRemovedFalse(request.carModelId())
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
        return carRepository.findByIdAndRemovedFalse(id)
                .map(carMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Car", id));
    }

    public List<CarResponse> getAllCars() {
        return carRepository.findAllByRemovedFalse().stream()
                .map(carMapper::toResponse)
                .toList();
    }

    public List<CarResponse> getAvailableCars() {
        return carRepository.findAllByAvailableTrueAndRemovedFalse().stream()
                .map(carMapper::toResponse)
                .toList();
    }

    public List<CarResponse> getCarsAvailableForTestDrive() {
        return carRepository.findAllByAvailableForTestDriveTrueAndRemovedFalse().stream()
                .map(carMapper::toResponse)
                .toList();
    }

    public List<CarResponse> filterCars(CarFilterRequest filter) {
        return carRepository.findAll(CarSpecification.byFilter(filter)).stream()
                .map(carMapper::toResponse)
                .toList();
    }

    @Transactional
    public CarResponse updateCar(UUID id, UpdateCarRequest request) {
        validateUpdateRequest(request);

        Car car = carRepository.findByIdAndRemovedFalse(id)
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

    @Transactional
    public void deleteCar(UUID id) {
        Car car = carRepository.findByIdAndRemovedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Car", id));

        car.markAsRemoved();
        carRepository.save(car);
    }

    private void validateCreateRequest(CreateCarRequest request) {
        if (request == null) {
            throw new DomainValidationException("Create car request must not be null");
        }

        if (request.carModelId() == null) {
            throw new DomainValidationException("Car model id must not be null");
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

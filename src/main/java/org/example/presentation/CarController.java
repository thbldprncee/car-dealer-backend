package org.example.presentation;

import org.example.dto.request.CarFilterRequest;
import org.example.dto.request.CreateCarRequest;
import org.example.dto.request.UpdateCarRequest;
import org.example.dto.response.CarResponse;
import org.example.service.CarService;

import java.util.List;
import java.util.UUID;

public class CarController {

    private final CarService carService;

    public CarController(CarService carService) {
        this.carService = carService;
    }

    public CarResponse createCar(CreateCarRequest request) {
        return carService.createCar(request);
    }

    public CarResponse getCarById(UUID id) {
        return carService.getCarById(id);
    }

    public List<CarResponse> getAllCars() {
        return carService.getAllCars();
    }

    public List<CarResponse> getAvailableCars() {
        return carService.getAvailableCars();
    }

    public List<CarResponse> getCarsAvailableForTestDrive() {
        return carService.getCarsAvailableForTestDrive();
    }

    public List<CarResponse> filterCars(CarFilterRequest filter) {
        return carService.filterCars(filter);
    }

    public CarResponse updateCar(UUID id, UpdateCarRequest request) {
        return carService.updateCar(id, request);
    }

    public void deleteCar(UUID id) {
        carService.deleteCar(id);
    }
}
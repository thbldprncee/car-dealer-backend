package org.example.presentation;

import lombok.RequiredArgsConstructor;
import org.example.dto.request.CarFilterRequest;
import org.example.dto.request.CreateCarRequest;
import org.example.dto.request.UpdateCarRequest;
import org.example.dto.response.CarResponse;
import org.example.service.CarService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cars")
@RequiredArgsConstructor
public class CarController {

    private final CarService carService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CarResponse createCar(@RequestBody CreateCarRequest request) {
        return carService.createCar(request);
    }

    @GetMapping("/{id}")
    public CarResponse getCarById(@PathVariable UUID id) {
        return carService.getCarById(id);
    }

    @GetMapping
    public List<CarResponse> getAllCars() {
        return carService.getAllCars();
    }

    @GetMapping("/available")
    public List<CarResponse> getAvailableCars() {
        return carService.getAvailableCars();
    }

    @GetMapping("/test-drive")
    public List<CarResponse> getCarsAvailableForTestDrive() {
        return carService.getCarsAvailableForTestDrive();
    }

    @PostMapping("/filter")
    public List<CarResponse> filterCars(@RequestBody(required = false) CarFilterRequest filter) {
        return carService.filterCars(filter);
    }

    @PutMapping("/{id}")
    public CarResponse updateCar(
            @PathVariable UUID id,
            @RequestBody UpdateCarRequest request
    ) {
        return carService.updateCar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCar(@PathVariable UUID id) {
        carService.deleteCar(id);
    }
}
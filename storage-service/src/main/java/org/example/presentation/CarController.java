package org.example.presentation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.request.CarFilterRequest;
import org.example.dto.request.CreateCarRequest;
import org.example.dto.request.UpdateCarRequest;
import org.example.dto.response.CarResponse;
import org.example.service.CarService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN', 'ADMIN')")
    public CarResponse createCar(@Valid @RequestBody CreateCarRequest request) {
        return carService.createCar(request);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'WAREHOUSE_ADMIN', 'ADMIN')")
    public CarResponse getCarById(@PathVariable UUID id) {
        return carService.getCarById(id);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'WAREHOUSE_ADMIN', 'ADMIN')")
    public List<CarResponse> getAllCars() {
        return carService.getAllCars();
    }

    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'WAREHOUSE_ADMIN', 'ADMIN')")
    public List<CarResponse> getAvailableCars() {
        return carService.getAvailableCars();
    }

    @GetMapping("/test-drive")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'WAREHOUSE_ADMIN', 'ADMIN')")
    public List<CarResponse> getCarsAvailableForTestDrive() {
        return carService.getCarsAvailableForTestDrive();
    }

    @PostMapping("/filter")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'WAREHOUSE_ADMIN', 'ADMIN')")
    public List<CarResponse> filterCars(@RequestBody(required = false) CarFilterRequest filter) {
        return carService.filterCars(filter);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN', 'ADMIN')")
    public CarResponse updateCar(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCarRequest request
    ) {
        return carService.updateCar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCar(@PathVariable UUID id) {
        carService.deleteCar(id);
    }
}

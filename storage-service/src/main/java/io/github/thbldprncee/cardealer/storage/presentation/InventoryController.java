package io.github.thbldprncee.cardealer.storage.presentation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.github.thbldprncee.cardealer.storage.dto.request.CarModelFilterRequest;
import io.github.thbldprncee.cardealer.storage.dto.request.CreateCarModelRequest;
import io.github.thbldprncee.cardealer.storage.dto.request.CreateCarRequest;
import io.github.thbldprncee.cardealer.storage.dto.request.CreateComponentOptionRequest;
import io.github.thbldprncee.cardealer.storage.dto.request.CreateSparePartRequest;
import io.github.thbldprncee.cardealer.storage.dto.request.UpdateCarRequest;
import io.github.thbldprncee.cardealer.storage.dto.request.UpdateSparePartRequest;
import io.github.thbldprncee.cardealer.storage.dto.response.CarModelResponse;
import io.github.thbldprncee.cardealer.storage.dto.response.CarResponse;
import io.github.thbldprncee.cardealer.storage.dto.response.ComponentOptionResponse;
import io.github.thbldprncee.cardealer.storage.dto.response.SparePartResponse;
import io.github.thbldprncee.cardealer.storage.service.InventoryService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/car-models")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN', 'ADMIN')")
    public CarModelResponse createCarModel(@Valid @RequestBody CreateCarModelRequest request) {
        return inventoryService.createCarModel(request);
    }

    @GetMapping("/car-models/{id}")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'WAREHOUSE_ADMIN', 'ADMIN')")
    public CarModelResponse getCarModelById(@PathVariable UUID id) {
        return inventoryService.getCarModelById(id);
    }

    @GetMapping("/car-models")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'WAREHOUSE_ADMIN', 'ADMIN')")
    public List<CarModelResponse> getAllCarModels() {
        return inventoryService.getAllCarModels();
    }

    @PostMapping("/car-models/filter")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'WAREHOUSE_ADMIN', 'ADMIN')")
    public List<CarModelResponse> filterBaseCarConfigurations(
            @RequestBody(required = false) CarModelFilterRequest request
    ) {
        return inventoryService.filterBaseCarConfigurations(request);
    }

    @DeleteMapping("/car-models/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCarModel(@PathVariable UUID id) {
        inventoryService.deleteCarModel(id);
    }

    @PostMapping("/component-options")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN', 'ADMIN')")
    public ComponentOptionResponse createComponentOption(@Valid @RequestBody CreateComponentOptionRequest request) {
        return inventoryService.createComponentOption(request);
    }

    @GetMapping("/component-options/{id}")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'WAREHOUSE_ADMIN', 'ADMIN')")
    public ComponentOptionResponse getComponentOptionById(@PathVariable UUID id) {
        return inventoryService.getComponentOptionById(id);
    }

    @GetMapping("/component-options")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'WAREHOUSE_ADMIN', 'ADMIN')")
    public List<ComponentOptionResponse> getAllComponentOptions() {
        return inventoryService.getAllComponentOptions();
    }

    @DeleteMapping("/component-options/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteComponentOption(@PathVariable UUID id) {
        inventoryService.deleteComponentOption(id);
    }

    @PostMapping("/cars")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN', 'ADMIN')")
    public CarResponse createCar(@Valid @RequestBody CreateCarRequest request) {
        return inventoryService.createCar(request);
    }

    @GetMapping("/cars/{id}")
    @PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN', 'ADMIN')")
    public CarResponse getCarById(@PathVariable UUID id) {
        return inventoryService.getCarById(id);
    }

    @GetMapping("/cars")
    @PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN', 'ADMIN')")
    public List<CarResponse> getAllCars() {
        return inventoryService.getAllCars();
    }

    @PutMapping("/cars/{id}")
    @PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN', 'ADMIN')")
    public CarResponse updateCar(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCarRequest request
    ) {
        return inventoryService.updateCar(id, request);
    }

    @DeleteMapping("/cars/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCar(@PathVariable UUID id) {
        inventoryService.deleteCar(id);
    }

    @PostMapping("/spare-parts")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN', 'ADMIN')")
    public SparePartResponse createSparePart(@Valid @RequestBody CreateSparePartRequest request) {
        return inventoryService.createSparePart(request);
    }

    @GetMapping("/spare-parts/{id}")
    @PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN', 'ADMIN')")
    public SparePartResponse getSparePartById(@PathVariable UUID id) {
        return inventoryService.getSparePartById(id);
    }

    @GetMapping("/spare-parts")
    @PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN', 'ADMIN')")
    public List<SparePartResponse> getAllSpareParts() {
        return inventoryService.getAllSpareParts();
    }

    @PutMapping("/spare-parts/{id}")
    @PreAuthorize("hasAnyRole('WAREHOUSE_ADMIN', 'ADMIN')")
    public SparePartResponse updateSparePart(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSparePartRequest request
    ) {
        return inventoryService.updateSparePart(id, request);
    }

    @DeleteMapping("/spare-parts/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteSparePart(@PathVariable UUID id) {
        inventoryService.deleteSparePart(id);
    }
}

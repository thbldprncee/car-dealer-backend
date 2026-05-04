package org.example.presentation;

import lombok.RequiredArgsConstructor;
import org.example.dto.request.CarModelFilterRequest;
import org.example.dto.request.CreateCarModelRequest;
import org.example.dto.request.CreateCarRequest;
import org.example.dto.request.CreateComponentOptionRequest;
import org.example.dto.request.CreateSparePartRequest;
import org.example.dto.request.UpdateCarRequest;
import org.example.dto.request.UpdateSparePartRequest;
import org.example.dto.response.CarModelResponse;
import org.example.dto.response.CarResponse;
import org.example.dto.response.ComponentOptionResponse;
import org.example.dto.response.SparePartResponse;
import org.example.service.InventoryService;
import org.springframework.http.HttpStatus;
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
    public CarModelResponse createCarModel(@RequestBody CreateCarModelRequest request) {
        return inventoryService.createCarModel(request);
    }

    @GetMapping("/car-models/{id}")
    public CarModelResponse getCarModelById(@PathVariable UUID id) {
        return inventoryService.getCarModelById(id);
    }

    @GetMapping("/car-models")
    public List<CarModelResponse> getAllCarModels() {
        return inventoryService.getAllCarModels();
    }

    @PostMapping("/car-models/filter")
    public List<CarModelResponse> filterBaseCarConfigurations(
            @RequestBody(required = false) CarModelFilterRequest request
    ) {
        return inventoryService.filterBaseCarConfigurations(request);
    }

    @DeleteMapping("/car-models/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCarModel(@PathVariable UUID id) {
        inventoryService.deleteCarModel(id);
    }

    @PostMapping("/component-options")
    @ResponseStatus(HttpStatus.CREATED)
    public ComponentOptionResponse createComponentOption(@RequestBody CreateComponentOptionRequest request) {
        return inventoryService.createComponentOption(request);
    }

    @GetMapping("/component-options/{id}")
    public ComponentOptionResponse getComponentOptionById(@PathVariable UUID id) {
        return inventoryService.getComponentOptionById(id);
    }

    @GetMapping("/component-options")
    public List<ComponentOptionResponse> getAllComponentOptions() {
        return inventoryService.getAllComponentOptions();
    }

    @DeleteMapping("/component-options/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComponentOption(@PathVariable UUID id) {
        inventoryService.deleteComponentOption(id);
    }

    @PostMapping("/cars")
    @ResponseStatus(HttpStatus.CREATED)
    public CarResponse createCar(@RequestBody CreateCarRequest request) {
        return inventoryService.createCar(request);
    }

    @GetMapping("/cars/{id}")
    public CarResponse getCarById(@PathVariable UUID id) {
        return inventoryService.getCarById(id);
    }

    @GetMapping("/cars")
    public List<CarResponse> getAllCars() {
        return inventoryService.getAllCars();
    }

    @PutMapping("/cars/{id}")
    public CarResponse updateCar(
            @PathVariable UUID id,
            @RequestBody UpdateCarRequest request
    ) {
        return inventoryService.updateCar(id, request);
    }

    @DeleteMapping("/cars/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCar(@PathVariable UUID id) {
        inventoryService.deleteCar(id);
    }

    @PostMapping("/spare-parts")
    @ResponseStatus(HttpStatus.CREATED)
    public SparePartResponse createSparePart(@RequestBody CreateSparePartRequest request) {
        return inventoryService.createSparePart(request);
    }

    @GetMapping("/spare-parts/{id}")
    public SparePartResponse getSparePartById(@PathVariable UUID id) {
        return inventoryService.getSparePartById(id);
    }

    @GetMapping("/spare-parts")
    public List<SparePartResponse> getAllSpareParts() {
        return inventoryService.getAllSpareParts();
    }

    @PutMapping("/spare-parts/{id}")
    public SparePartResponse updateSparePart(
            @PathVariable UUID id,
            @RequestBody UpdateSparePartRequest request
    ) {
        return inventoryService.updateSparePart(id, request);
    }

    @DeleteMapping("/spare-parts/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSparePart(@PathVariable UUID id) {
        inventoryService.deleteSparePart(id);
    }
}
package org.example.presentation;

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

import java.util.List;
import java.util.UUID;

public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    public CarModelResponse createCarModel(CreateCarModelRequest request) {
        return inventoryService.createCarModel(request);
    }

    public ComponentOptionResponse createComponentOption(CreateComponentOptionRequest request) {
        return inventoryService.createComponentOption(request);
    }

    public CarResponse createCar(CreateCarRequest request) {
        return inventoryService.createCar(request);
    }

    public SparePartResponse createSparePart(CreateSparePartRequest request) {
        return inventoryService.createSparePart(request);
    }

    public CarResponse getCarById(UUID id) {
        return inventoryService.getCarById(id);
    }

    public SparePartResponse getSparePartById(UUID id) {
        return inventoryService.getSparePartById(id);
    }

    public CarModelResponse getCarModelById(UUID id) {
        return inventoryService.getCarModelById(id);
    }

    public ComponentOptionResponse getComponentOptionById(UUID id) {
        return inventoryService.getComponentOptionById(id);
    }

    public List<CarResponse> getAllCars() {
        return inventoryService.getAllCars();
    }

    public List<SparePartResponse> getAllSpareParts() {
        return inventoryService.getAllSpareParts();
    }

    public List<CarModelResponse> getAllCarModels() {
        return inventoryService.getAllCarModels();
    }

    public List<ComponentOptionResponse> getAllComponentOptions() {
        return inventoryService.getAllComponentOptions();
    }

    public CarResponse updateCar(UUID id, UpdateCarRequest request) {
        return inventoryService.updateCar(id, request);
    }

    public SparePartResponse updateSparePart(UUID id, UpdateSparePartRequest request) {
        return inventoryService.updateSparePart(id, request);
    }

    public void deleteCar(UUID id) {
        inventoryService.deleteCar(id);
    }

    public void deleteSparePart(UUID id) {
        inventoryService.deleteSparePart(id);
    }

    public void deleteCarModel(UUID id) {
        inventoryService.deleteCarModel(id);
    }

    public void deleteComponentOption(UUID id) {
        inventoryService.deleteComponentOption(id);
    }
}
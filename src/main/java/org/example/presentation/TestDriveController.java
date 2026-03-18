package org.example.presentation;

import org.example.dto.request.CreateTestDriveRequest;
import org.example.dto.response.CarResponse;
import org.example.dto.response.TestDriveRequestResponse;
import org.example.service.TestDriveService;

import java.util.List;
import java.util.UUID;

public class TestDriveController {

    private final TestDriveService testDriveService;

    public TestDriveController(TestDriveService testDriveService) {
        this.testDriveService = testDriveService;
    }

    public TestDriveRequestResponse createTestDriveRequest(CreateTestDriveRequest request) {
        return testDriveService.createTestDriveRequest(request);
    }

    public TestDriveRequestResponse getTestDriveRequestById(UUID id) {
        return testDriveService.getTestDriveRequestById(id);
    }

    public List<TestDriveRequestResponse> getAllTestDriveRequests() {
        return testDriveService.getAllTestDriveRequests();
    }

    public List<CarResponse> getCarsAvailableForTestDrive() {
        return testDriveService.getCarsAvailableForTestDrive();
    }

    public CarResponse addCarToTestDriveList(UUID carId) {
        return testDriveService.addCarToTestDriveList(carId);
    }

    public CarResponse removeCarFromTestDriveList(UUID carId) {
        return testDriveService.removeCarFromTestDriveList(carId);
    }

    public TestDriveRequestResponse approveTestDriveRequest(UUID requestId) {
        return testDriveService.approveTestDriveRequest(requestId);
    }

    public TestDriveRequestResponse completeTestDriveRequest(UUID requestId) {
        return testDriveService.completeTestDriveRequest(requestId);
    }

    public TestDriveRequestResponse cancelTestDriveRequest(UUID requestId) {
        return testDriveService.cancelTestDriveRequest(requestId);
    }
}
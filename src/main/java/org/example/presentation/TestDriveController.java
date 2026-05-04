package org.example.presentation;

import lombok.RequiredArgsConstructor;
import org.example.domain.enums.TestDriveStatus;
import org.example.dto.request.CreateTestDriveRequest;
import org.example.dto.response.CarResponse;
import org.example.dto.response.TestDriveRequestResponse;
import org.example.service.TestDriveService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/test-drives")
@RequiredArgsConstructor
public class TestDriveController {

    private final TestDriveService testDriveService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TestDriveRequestResponse createTestDriveRequest(
            @RequestBody CreateTestDriveRequest request
    ) {
        return testDriveService.createTestDriveRequest(request);
    }

    @GetMapping("/{id}")
    public TestDriveRequestResponse getTestDriveRequestById(@PathVariable UUID id) {
        return testDriveService.getTestDriveRequestById(id);
    }

    @GetMapping
    public List<TestDriveRequestResponse> getAllTestDriveRequests(
            @RequestParam(required = false) TestDriveStatus status
    ) {
        if (status == null) {
            return testDriveService.getAllTestDriveRequests();
        }

        return testDriveService.getTestDriveRequestsByStatus(status);
    }

    @GetMapping("/cars")
    public List<CarResponse> getCarsAvailableForTestDrive() {
        return testDriveService.getCarsAvailableForTestDrive();
    }

    @PatchMapping("/cars/{carId}/add")
    public CarResponse addCarToTestDriveList(@PathVariable UUID carId) {
        return testDriveService.addCarToTestDriveList(carId);
    }

    @PatchMapping("/cars/{carId}/remove")
    public CarResponse removeCarFromTestDriveList(@PathVariable UUID carId) {
        return testDriveService.removeCarFromTestDriveList(carId);
    }

    @PatchMapping("/{id}/approve")
    public TestDriveRequestResponse approveTestDriveRequest(@PathVariable UUID id) {
        return testDriveService.approveTestDriveRequest(id);
    }

    @PatchMapping("/{id}/complete")
    public TestDriveRequestResponse completeTestDriveRequest(@PathVariable UUID id) {
        return testDriveService.completeTestDriveRequest(id);
    }

    @PatchMapping("/{id}/cancel")
    public TestDriveRequestResponse cancelTestDriveRequest(@PathVariable UUID id) {
        return testDriveService.cancelTestDriveRequest(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTestDriveRequest(@PathVariable UUID id) {
        testDriveService.deleteTestDriveRequest(id);
    }
}
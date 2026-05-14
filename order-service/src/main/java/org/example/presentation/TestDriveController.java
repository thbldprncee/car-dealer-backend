package org.example.presentation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.domain.enums.TestDriveStatus;
import org.example.dto.request.CreateTestDriveRequest;
import org.example.dto.response.TestDriveRequestResponse;
import org.example.service.TestDriveService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public TestDriveRequestResponse createTestDriveRequest(
            @Valid @RequestBody CreateTestDriveRequest request
    ) {
        return testDriveService.createTestDriveRequest(request);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN') or @orderSecurityService.isTestDriveRequestOwner(#id)")
    public TestDriveRequestResponse getTestDriveRequestById(@PathVariable UUID id) {
        return testDriveService.getTestDriveRequestById(id);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public List<TestDriveRequestResponse> getAllTestDriveRequests(
            @RequestParam(required = false) TestDriveStatus status
    ) {
        if (status == null) {
            return testDriveService.getAllTestDriveRequests();
        }

        return testDriveService.getTestDriveRequestsByStatus(status);
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public TestDriveRequestResponse approveTestDriveRequest(@PathVariable UUID id) {
        return testDriveService.approveTestDriveRequest(id);
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public TestDriveRequestResponse completeTestDriveRequest(@PathVariable UUID id) {
        return testDriveService.completeTestDriveRequest(id);
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN') or @orderSecurityService.isTestDriveRequestOwner(#id)")
    public TestDriveRequestResponse cancelTestDriveRequest(@PathVariable UUID id) {
        return testDriveService.cancelTestDriveRequest(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteTestDriveRequest(@PathVariable UUID id) {
        testDriveService.deleteTestDriveRequest(id);
    }
}

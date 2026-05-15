package org.example.presentation;

import lombok.RequiredArgsConstructor;
import org.example.dto.response.AvailableCarResponse;
import org.example.service.AvailableCarService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cars")
@RequiredArgsConstructor
public class AvailableCarController {

    private final AvailableCarService availableCarService;

    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENT', 'SALES_MANAGER', 'ADMIN')")
    public List<AvailableCarResponse> getAvailableCars() {
        return availableCarService.getAvailableCars();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT', 'SALES_MANAGER', 'ADMIN')")
    public AvailableCarResponse getAvailableCarById(@PathVariable UUID id) {
        return availableCarService.getAvailableCarById(id);
    }
}

package org.example.presentation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.request.BuildCarConfigurationRequest;
import org.example.dto.response.CarConfigurationResponse;
import org.example.service.ConfiguratorService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/configurations")
@RequiredArgsConstructor
public class ConfiguratorController {

    private final ConfiguratorService configuratorService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public CarConfigurationResponse buildConfiguration(
            @Valid @RequestBody BuildCarConfigurationRequest request
    ) {
        return configuratorService.buildConfiguration(request);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public CarConfigurationResponse getConfigurationById(@PathVariable UUID id) {
        return configuratorService.getConfigurationById(id);
    }
}

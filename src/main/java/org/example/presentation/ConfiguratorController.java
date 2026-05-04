package org.example.presentation;

import lombok.RequiredArgsConstructor;
import org.example.dto.request.BuildCarConfigurationRequest;
import org.example.dto.response.CarConfigurationResponse;
import org.example.service.ConfiguratorService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/configurations")
@RequiredArgsConstructor
public class ConfiguratorController {

    private final ConfiguratorService configuratorService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CarConfigurationResponse buildConfiguration(
            @RequestBody BuildCarConfigurationRequest request
    ) {
        return configuratorService.buildConfiguration(request);
    }

    @GetMapping("/{id}")
    public CarConfigurationResponse getConfigurationById(@PathVariable UUID id) {
        return configuratorService.getConfigurationById(id);
    }
}
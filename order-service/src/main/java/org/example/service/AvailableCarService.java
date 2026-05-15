package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.client.CarInventoryClient;
import org.example.dto.response.AvailableCarResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AvailableCarService {

    private final CarInventoryClient carInventoryClient;

    public List<AvailableCarResponse> getAvailableCars() {
        return carInventoryClient.getAvailableCars();
    }

    public AvailableCarResponse getAvailableCarById(UUID id) {
        return carInventoryClient.getAvailableCarById(id);
    }
}

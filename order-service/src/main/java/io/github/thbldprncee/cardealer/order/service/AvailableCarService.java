package io.github.thbldprncee.cardealer.order.service;

import lombok.RequiredArgsConstructor;
import io.github.thbldprncee.cardealer.order.client.CarInventoryClient;
import io.github.thbldprncee.cardealer.order.dto.response.AvailableCarResponse;
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

package io.github.thbldprncee.cardealer.order.client;

import io.github.thbldprncee.cardealer.order.dto.response.AvailableCarResponse;

import java.util.List;
import java.util.UUID;

public interface CarInventoryClient {

    List<AvailableCarResponse> getAvailableCars();

    AvailableCarResponse getAvailableCarById(UUID id);
}

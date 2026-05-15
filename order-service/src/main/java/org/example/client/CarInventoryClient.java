package org.example.client;

import org.example.dto.response.AvailableCarResponse;

import java.util.List;
import java.util.UUID;

public interface CarInventoryClient {

    List<AvailableCarResponse> getAvailableCars();

    AvailableCarResponse getAvailableCarById(UUID id);
}

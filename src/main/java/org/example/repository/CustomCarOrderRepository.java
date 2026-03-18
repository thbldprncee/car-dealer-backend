package org.example.repository;

import org.example.domain.model.CustomCarOrder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomCarOrderRepository {

    CustomCarOrder save(CustomCarOrder order);

    Optional<CustomCarOrder> findById(UUID id);

    List<CustomCarOrder> findAll();

    void deleteById(UUID id);

    boolean existsById(UUID id);
}
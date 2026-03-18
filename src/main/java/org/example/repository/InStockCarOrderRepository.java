package org.example.repository;

import org.example.domain.model.InStockCarOrder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InStockCarOrderRepository {

    InStockCarOrder save(InStockCarOrder order);

    Optional<InStockCarOrder> findById(UUID id);

    List<InStockCarOrder> findAll();

    void deleteById(UUID id);

    boolean existsById(UUID id);
}
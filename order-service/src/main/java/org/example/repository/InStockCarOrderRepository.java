package org.example.repository;

import org.example.domain.enums.InStockOrderStatus;
import org.example.domain.model.InStockCarOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InStockCarOrderRepository extends JpaRepository<InStockCarOrder, UUID> {

    Optional<InStockCarOrder> findByIdAndRemovedFalse(UUID id);

    List<InStockCarOrder> findAllByRemovedFalse();

    List<InStockCarOrder> findAllByStatusAndRemovedFalse(InStockOrderStatus status);

    List<InStockCarOrder> findAllByClientIdAndRemovedFalse(UUID clientId);

    List<InStockCarOrder> findAllByClientIdAndStatusAndRemovedFalse(UUID clientId, InStockOrderStatus status);

    List<InStockCarOrder> findAllBySalesManagerIdAndRemovedFalse(UUID salesManagerId);
}

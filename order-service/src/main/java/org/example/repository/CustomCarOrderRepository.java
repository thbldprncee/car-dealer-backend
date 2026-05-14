package org.example.repository;

import org.example.domain.enums.CustomOrderStatus;
import org.example.domain.model.CustomCarOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomCarOrderRepository extends JpaRepository<CustomCarOrder, UUID> {

    Optional<CustomCarOrder> findByIdAndRemovedFalse(UUID id);

    List<CustomCarOrder> findAllByRemovedFalse();

    List<CustomCarOrder> findAllByStatusAndRemovedFalse(CustomOrderStatus status);

    List<CustomCarOrder> findAllByClientIdAndRemovedFalse(UUID clientId);

    List<CustomCarOrder> findAllByClientIdAndStatusAndRemovedFalse(UUID clientId, CustomOrderStatus status);

    List<CustomCarOrder> findAllBySalesManagerIdAndRemovedFalse(UUID salesManagerId);
}

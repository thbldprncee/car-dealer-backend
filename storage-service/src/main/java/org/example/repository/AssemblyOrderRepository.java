package org.example.repository;

import org.example.domain.model.AssemblyOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssemblyOrderRepository extends JpaRepository<AssemblyOrder, UUID> {

    Optional<AssemblyOrder> findByIdAndRemovedFalse(UUID id);

    List<AssemblyOrder> findAllByRemovedFalse();

    Optional<AssemblyOrder> findBySourceOrderIdAndRemovedFalse(UUID sourceOrderId);
}

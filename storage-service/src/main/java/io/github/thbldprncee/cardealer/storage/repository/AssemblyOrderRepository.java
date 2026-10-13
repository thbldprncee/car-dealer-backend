package io.github.thbldprncee.cardealer.storage.repository;

import io.github.thbldprncee.cardealer.storage.domain.model.AssemblyOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssemblyOrderRepository extends JpaRepository<AssemblyOrder, UUID> {

    Optional<AssemblyOrder> findByIdAndRemovedFalse(UUID id);

    List<AssemblyOrder> findAllByRemovedFalse();

    Optional<AssemblyOrder> findBySourceOrderIdAndRemovedFalse(UUID sourceOrderId);
}

package io.github.thbldprncee.cardealer.storage.repository;

import io.github.thbldprncee.cardealer.storage.domain.model.SparePart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SparePartRepository extends JpaRepository<SparePart, UUID> {

    List<SparePart> findAllByRemovedFalse();

    Optional<SparePart> findByIdAndRemovedFalse(UUID id);

    Optional<SparePart> findByNameIgnoreCaseAndRemovedFalse(String name);
}

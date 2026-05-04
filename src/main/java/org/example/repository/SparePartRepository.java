package org.example.repository;

import org.example.domain.model.SparePart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SparePartRepository extends JpaRepository<SparePart, UUID> {

    List<SparePart> findAllByRemovedFalse();

    Optional<SparePart> findByNameIgnoreCaseAndRemovedFalse(String name);
}
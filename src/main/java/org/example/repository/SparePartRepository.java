package org.example.repository;

import org.example.domain.model.SparePart;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SparePartRepository {

    SparePart save(SparePart sparePart);

    Optional<SparePart> findById(UUID id);

    List<SparePart> findAll();

    void deleteById(UUID id);

    boolean existsById(UUID id);
}
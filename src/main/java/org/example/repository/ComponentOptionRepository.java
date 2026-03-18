package org.example.repository;

import org.example.domain.enums.ComponentType;
import org.example.domain.model.ComponentOption;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ComponentOptionRepository {

    ComponentOption save(ComponentOption componentOption);

    Optional<ComponentOption> findById(UUID id);

    List<ComponentOption> findAll();

    void deleteById(UUID id);

    boolean existsById(UUID id);

    List<ComponentOption> findByComponentType(ComponentType componentType);
}
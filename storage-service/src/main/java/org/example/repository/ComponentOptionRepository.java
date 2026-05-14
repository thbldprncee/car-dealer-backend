package org.example.repository;

import org.example.domain.enums.ComponentType;
import org.example.domain.model.ComponentOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ComponentOptionRepository extends JpaRepository<ComponentOption, UUID> {

    List<ComponentOption> findAllByRemovedFalse();

    List<ComponentOption> findAllByTypeAndRemovedFalse(ComponentType type);

    Optional<ComponentOption> findByIdAndRemovedFalse(UUID id);

    Optional<ComponentOption> findByNameIgnoreCaseAndRemovedFalse(String name);
}

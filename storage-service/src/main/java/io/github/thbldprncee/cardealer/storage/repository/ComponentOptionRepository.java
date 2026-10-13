package io.github.thbldprncee.cardealer.storage.repository;

import io.github.thbldprncee.cardealer.storage.domain.enums.ComponentType;
import io.github.thbldprncee.cardealer.storage.domain.model.ComponentOption;
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

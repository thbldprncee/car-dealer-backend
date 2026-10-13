package io.github.thbldprncee.cardealer.storage.repository;

import io.github.thbldprncee.cardealer.storage.domain.model.CarConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CarConfigurationRepository extends JpaRepository<CarConfiguration, UUID> {

    List<CarConfiguration> findAllByRemovedFalse();

    Optional<CarConfiguration> findByIdAndRemovedFalse(UUID id);
}

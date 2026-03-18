package org.example.repository;

import org.example.domain.model.CarConfiguration;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CarConfigurationRepository {

    CarConfiguration save(CarConfiguration configuration);

    Optional<CarConfiguration> findById(UUID id);

    List<CarConfiguration> findAll();

    void deleteById(UUID id);

    boolean existsById(UUID id);
}
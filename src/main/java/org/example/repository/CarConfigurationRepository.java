package org.example.repository;

import org.example.domain.model.CarConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CarConfigurationRepository extends JpaRepository<CarConfiguration, UUID> {

    List<CarConfiguration> findAllByRemovedFalse();
}
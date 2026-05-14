package org.example.repository;

import org.example.domain.model.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CarRepository extends JpaRepository<Car, UUID>, JpaSpecificationExecutor<Car> {

    List<Car> findAllByRemovedFalse();

    List<Car> findAllByAvailableTrueAndRemovedFalse();

    List<Car> findAllByAvailableForTestDriveTrueAndRemovedFalse();

    Optional<Car> findByIdAndRemovedFalse(UUID id);

    Optional<Car> findByVinAndRemovedFalse(String vin);
}

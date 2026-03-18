package org.example.repository;

import org.example.domain.model.Car;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CarRepository {

    Car save(Car car);

    Optional<Car> findById(UUID id);

    List<Car> findAll();

    void deleteById(UUID id);

    boolean existsById(UUID id);

    Optional<Car> findByVin(String vin);

    List<Car> findAvailableCars();

    List<Car> findCarsAvailableForTestDrive();
}
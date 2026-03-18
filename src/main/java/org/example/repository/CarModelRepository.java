package org.example.repository;

import org.example.domain.model.CarModel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CarModelRepository {

    CarModel save(CarModel carModel);

    Optional<CarModel> findById(UUID id);

    List<CarModel> findAll();

    void deleteById(UUID id);

    boolean existsById(UUID id);
}
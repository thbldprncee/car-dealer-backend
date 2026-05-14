package org.example.repository;

import org.example.domain.model.CarModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CarModelRepository extends JpaRepository<CarModel, UUID>, JpaSpecificationExecutor<CarModel> {

    List<CarModel> findAllByRemovedFalse();

    List<CarModel> findAllByBrandIgnoreCaseAndRemovedFalse(String brand);

    Optional<CarModel> findByIdAndRemovedFalse(UUID id);

    Optional<CarModel> findByBrandIgnoreCaseAndModelNameIgnoreCaseAndRemovedFalse(
            String brand,
            String modelName
    );
}

package org.example.repository.specification;

import jakarta.persistence.criteria.Join;
import org.example.domain.model.Car;
import org.example.domain.model.CarModel;
import org.example.dto.request.CarFilterRequest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public final class CarSpecification {

    private CarSpecification() {
    }

    public static Specification<Car> byFilter(CarFilterRequest filter) {
        return available()
                .and(notRemoved())
                .and(hasMinPrice(filter == null ? null : filter.minPrice()))
                .and(hasMaxPrice(filter == null ? null : filter.maxPrice()))
                .and(hasBrand(filter == null ? null : filter.brand()))
                .and(hasModelName(filter == null ? null : filter.modelName()))
                .and(hasCarModelAttribute("bodyType", filter == null ? null : filter.bodyType()))
                .and(hasCarModelAttribute("fuelType", filter == null ? null : filter.fuelType()))
                .and(hasMinCarModelNumber("enginePower", filter == null ? null : filter.minEnginePower()))
                .and(hasMaxCarModelNumber("enginePower", filter == null ? null : filter.maxEnginePower()))
                .and(hasMinCarModelNumber("engineVolume", filter == null ? null : filter.minEngineVolume()))
                .and(hasMaxCarModelNumber("engineVolume", filter == null ? null : filter.maxEngineVolume()))
                .and(hasCarModelAttribute("transmissionType", filter == null ? null : filter.transmissionType()))
                .and(hasCarModelAttribute("driveType", filter == null ? null : filter.driveType()))
                .and(hasAttribute("color", filter == null ? null : filter.color()));
    }

    private static Specification<Car> notRemoved() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isFalse(root.get("removed"));
    }

    private static Specification<Car> available() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isTrue(root.get("available"));
    }

    private static Specification<Car> hasMinPrice(BigDecimal value) {
        return (root, query, criteriaBuilder) -> value == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.greaterThanOrEqualTo(root.get("price"), value);
    }

    private static Specification<Car> hasMaxPrice(BigDecimal value) {
        return (root, query, criteriaBuilder) -> value == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.lessThanOrEqualTo(root.get("price"), value);
    }

    private static Specification<Car> hasBrand(String brand) {
        return (root, query, criteriaBuilder) -> {
            if (brand == null || brand.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            Join<Car, CarModel> carModel = root.join("carModel");
            return criteriaBuilder.equal(criteriaBuilder.lower(carModel.get("brand")), brand.trim().toLowerCase());
        };
    }

    private static Specification<Car> hasModelName(String modelName) {
        return (root, query, criteriaBuilder) -> {
            if (modelName == null || modelName.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            Join<Car, CarModel> carModel = root.join("carModel");
            return criteriaBuilder.equal(
                    criteriaBuilder.lower(carModel.get("modelName")),
                    modelName.trim().toLowerCase()
            );
        };
    }

    private static Specification<Car> hasAttribute(String attribute, Object value) {
        return (root, query, criteriaBuilder) -> value == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get(attribute), value);
    }

    private static Specification<Car> hasCarModelAttribute(String attribute, Object value) {
        return (root, query, criteriaBuilder) -> {
            if (value == null) {
                return criteriaBuilder.conjunction();
            }

            Join<Car, CarModel> carModel = root.join("carModel");
            return criteriaBuilder.equal(carModel.get(attribute), value);
        };
    }

    private static Specification<Car> hasMinCarModelNumber(String attribute, Number value) {
        return (root, query, criteriaBuilder) -> {
            if (value == null) {
                return criteriaBuilder.conjunction();
            }

            Join<Car, CarModel> carModel = root.join("carModel");
            return criteriaBuilder.ge(carModel.get(attribute), value);
        };
    }

    private static Specification<Car> hasMaxCarModelNumber(String attribute, Number value) {
        return (root, query, criteriaBuilder) -> {
            if (value == null) {
                return criteriaBuilder.conjunction();
            }

            Join<Car, CarModel> carModel = root.join("carModel");
            return criteriaBuilder.le(carModel.get(attribute), value);
        };
    }
}

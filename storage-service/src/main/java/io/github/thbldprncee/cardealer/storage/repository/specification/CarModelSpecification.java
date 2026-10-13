package io.github.thbldprncee.cardealer.storage.repository.specification;

import jakarta.persistence.criteria.MapJoin;
import io.github.thbldprncee.cardealer.storage.domain.enums.ComponentType;
import io.github.thbldprncee.cardealer.storage.domain.model.CarModel;
import io.github.thbldprncee.cardealer.storage.domain.model.ComponentOption;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;
import java.util.UUID;

public final class CarModelSpecification {

    private CarModelSpecification() {
    }

    public static Specification<CarModel> notRemoved() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.isFalse(root.get("removed"));
    }

    public static Specification<CarModel> hasBrand(String brand) {
        return (root, query, criteriaBuilder) -> {
            if (brand == null || brand.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    criteriaBuilder.lower(root.get("brand")),
                    brand.trim().toLowerCase()
            );
        };
    }

    public static Specification<CarModel> hasBaseComponent(UUID componentOptionId) {
        return (root, query, criteriaBuilder) -> {
            if (componentOptionId == null) {
                return criteriaBuilder.conjunction();
            }

            query.distinct(true);

            MapJoin<CarModel, ComponentType, ComponentOption> baseComponents =
                    root.joinMap("baseComponents");

            return criteriaBuilder.equal(
                    baseComponents.value().get("id"),
                    componentOptionId
            );
        };
    }

    public static Specification<CarModel> hasAllBaseComponents(Collection<UUID> componentOptionIds) {
        Specification<CarModel> specification = alwaysTrue();

        if (componentOptionIds == null || componentOptionIds.isEmpty()) {
            return specification;
        }

        for (UUID componentOptionId : componentOptionIds) {
            specification = specification.and(hasBaseComponent(componentOptionId));
        }

        return specification;
    }

    private static Specification<CarModel> alwaysTrue() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
    }
}
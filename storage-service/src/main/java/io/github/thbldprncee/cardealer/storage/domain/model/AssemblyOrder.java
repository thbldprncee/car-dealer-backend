package io.github.thbldprncee.cardealer.storage.domain.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import io.github.thbldprncee.cardealer.storage.domain.enums.AssemblyOrderStatus;
import io.github.thbldprncee.cardealer.storage.domain.exceptions.DomainValidationException;
import io.github.thbldprncee.cardealer.events.OrderType;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Getter
@Entity
@Table(name = "assembly_orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AssemblyOrder extends BaseEntity {

    @Column(name = "source_order_id", nullable = false)
    private UUID sourceOrderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_order_type", nullable = false)
    private OrderType sourceOrderType;

    @Column(name = "car_id")
    private UUID carId;

    @Column(name = "car_model_id")
    private UUID carModelId;

    @ElementCollection
    @CollectionTable(
            name = "assembly_order_required_components",
            joinColumns = @JoinColumn(name = "assembly_order_id")
    )
    @Column(name = "component_option_id", nullable = false)
    private Set<UUID> requiredComponentOptionIds = new HashSet<>();

    @Column(name = "warehouse_admin_id")
    private UUID warehouseAdminId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AssemblyOrderStatus status;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "trace_id", nullable = false)
    private String traceId;

    public AssemblyOrder(
            UUID id,
            UUID sourceOrderId,
            OrderType sourceOrderType,
            UUID carId,
            UUID carModelId,
            Set<UUID> requiredComponentOptionIds,
            String traceId
    ) {
        this.id = Optional.ofNullable(id)
                .orElseThrow(() -> new DomainValidationException("Assembly order id must not be null"));
        this.sourceOrderId = Optional.ofNullable(sourceOrderId)
                .orElseThrow(() -> new DomainValidationException("Source order id must not be null"));
        this.sourceOrderType = Optional.ofNullable(sourceOrderType)
                .orElseThrow(() -> new DomainValidationException("Source order type must not be null"));
        this.carId = carId;
        this.carModelId = carModelId;
        this.requiredComponentOptionIds = Optional.ofNullable(requiredComponentOptionIds)
                .map(HashSet::new)
                .orElseGet(HashSet::new);
        this.traceId = Optional.ofNullable(traceId)
                .filter(value -> !value.isBlank())
                .orElseThrow(() -> new DomainValidationException("Trace id must not be blank"));
        this.status = AssemblyOrderStatus.CREATED;
    }

    public void markAsAssembled() {
        this.status = AssemblyOrderStatus.ASSEMBLED;
        this.failureReason = null;
    }

    public void markAsFailed(String reason) {
        this.status = AssemblyOrderStatus.FAIL;
        this.failureReason = Optional.ofNullable(reason)
                .filter(value -> !value.isBlank())
                .orElse("Assembly order failed");
    }

    public void assignWarehouseAdmin(UUID warehouseAdminId) {
        this.warehouseAdminId = Optional.ofNullable(warehouseAdminId)
                .orElseThrow(() -> new DomainValidationException("Warehouse admin id must not be null"));
    }
}

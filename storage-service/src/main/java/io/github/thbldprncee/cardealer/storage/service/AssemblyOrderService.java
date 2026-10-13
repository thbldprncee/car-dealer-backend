package io.github.thbldprncee.cardealer.storage.service;

import lombok.RequiredArgsConstructor;
import io.github.thbldprncee.cardealer.storage.domain.exceptions.EntityNotFoundException;
import io.github.thbldprncee.cardealer.storage.domain.model.AssemblyOrder;
import io.github.thbldprncee.cardealer.storage.dto.request.CreateAssemblyOrderRequest;
import io.github.thbldprncee.cardealer.storage.dto.request.UpdateAssemblyOrderRequest;
import io.github.thbldprncee.cardealer.storage.dto.response.AssemblyOrderResponse;
import io.github.thbldprncee.cardealer.storage.mappers.AssemblyOrderMapper;
import io.github.thbldprncee.cardealer.storage.repository.AssemblyOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssemblyOrderService {

    private final AssemblyOrderRepository assemblyOrderRepository;
    private final AssemblyOrderMapper assemblyOrderMapper;

    @Transactional
    public AssemblyOrderResponse createAssemblyOrder(CreateAssemblyOrderRequest request) {
        AssemblyOrder assemblyOrder = new AssemblyOrder(
                UUID.randomUUID(),
                request.sourceOrderId(),
                request.sourceOrderType(),
                request.carId(),
                request.carModelId(),
                request.requiredComponentOptionIds(),
                Optional.ofNullable(request.traceId()).orElse(UUID.randomUUID().toString())
        );

        return assemblyOrderMapper.toResponse(assemblyOrderRepository.save(assemblyOrder));
    }

    public List<AssemblyOrderResponse> getAllAssemblyOrders() {
        return assemblyOrderRepository.findAllByRemovedFalse().stream()
                .map(assemblyOrderMapper::toResponse)
                .toList();
    }

    public AssemblyOrderResponse getAssemblyOrderById(UUID id) {
        return assemblyOrderRepository.findByIdAndRemovedFalse(id)
                .map(assemblyOrderMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("AssemblyOrder", id));
    }

    @Transactional
    public AssemblyOrderResponse updateAssemblyOrder(UUID id, UpdateAssemblyOrderRequest request) {
        AssemblyOrder assemblyOrder = assemblyOrderRepository.findByIdAndRemovedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("AssemblyOrder", id));

        Optional.ofNullable(request.warehouseAdminId())
                .ifPresent(assemblyOrder::assignWarehouseAdmin);

        if (Boolean.TRUE.equals(request.assembled())) {
            assemblyOrder.markAsAssembled();
        } else if (request.failureReason() != null && !request.failureReason().isBlank()) {
            assemblyOrder.markAsFailed(request.failureReason());
        }

        return assemblyOrderMapper.toResponse(assemblyOrderRepository.save(assemblyOrder));
    }

    @Transactional
    public void deleteAssemblyOrder(UUID id) {
        AssemblyOrder assemblyOrder = assemblyOrderRepository.findByIdAndRemovedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("AssemblyOrder", id));
        assemblyOrder.markAsRemoved();
        assemblyOrderRepository.save(assemblyOrder);
    }
}

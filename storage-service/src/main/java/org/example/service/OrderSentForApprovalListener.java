package org.example.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.config.TraceIdProvider;
import org.example.domain.model.AssemblyOrder;
import org.example.domain.model.Car;
import org.example.domain.model.CarConfiguration;
import org.example.domain.model.ComponentOption;
import org.example.domain.model.ProcessedEvent;
import org.example.events.ApprovalResult;
import org.example.events.OrderApprovalResultEvent;
import org.example.events.OrderSentForApprovalEvent;
import org.example.events.OrderType;
import org.example.repository.AssemblyOrderRepository;
import org.example.repository.CarConfigurationRepository;
import org.example.repository.CarModelRepository;
import org.example.repository.CarRepository;
import org.example.repository.ComponentOptionRepository;
import org.example.repository.ProcessedEventRepository;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderSentForApprovalListener {

    @Value("${app.kafka.topics.order-approval-result:order.approval-result}")
    private String orderApprovalResultTopic;

    private final ObjectMapper objectMapper;
    private final ProcessedEventRepository processedEventRepository;
    private final AssemblyOrderRepository assemblyOrderRepository;
    private final CarRepository carRepository;
    private final CarModelRepository carModelRepository;
    private final CarConfigurationRepository carConfigurationRepository;
    private final ComponentOptionRepository componentOptionRepository;
    private final OutboxEventService outboxEventService;

    @KafkaListener(
            topics = "${app.kafka.topics.order-sent-for-approval:order.sent-for-approval}",
            groupId = "${spring.kafka.consumer.group-id:storage-service}"
    )
    @Transactional
    public void handle(String payload) throws JsonProcessingException {
        OrderSentForApprovalEvent event = objectMapper.readValue(payload, OrderSentForApprovalEvent.class);
        MDC.put(TraceIdProvider.TRACE_ID, event.traceId());
        try {
            log.info("Received order sent for approval event id={}, orderId={}, orderType={}, traceId={}",
                    event.eventId(), event.orderId(), event.orderType(), event.traceId());
            if (processedEventRepository.existsById(event.eventId())) {
                log.info("Skipping duplicate order sent for approval event id={}, traceId={}",
                        event.eventId(), event.traceId());
                return;
            }

            List<UUID> componentOptionIds = resolveComponentOptionIds(event);
            AssemblyOrder assemblyOrder = new AssemblyOrder(
                    UUID.randomUUID(),
                    event.orderId(),
                    event.orderType(),
                    event.carId(),
                    event.carModelId(),
                    new HashSet<>(componentOptionIds),
                    event.traceId()
            );

            ApprovalDecision decision = event.orderType() == OrderType.IN_STOCK
                    ? approveInStock(event)
                    : approveCustom(event, componentOptionIds);

            if (decision.approved()) {
                log.info("Warehouse approved orderId={}, orderType={}", event.orderId(), event.orderType());
                assemblyOrder.markAsAssembled();
            } else {
                log.info("Warehouse rejected orderId={}, orderType={}, reason={}",
                        event.orderId(), event.orderType(), decision.reason());
                assemblyOrder.markAsFailed(decision.reason());
            }

            assemblyOrderRepository.save(assemblyOrder);
            processedEventRepository.save(new ProcessedEvent(
                    event.eventId(),
                    OrderSentForApprovalEvent.class.getSimpleName(),
                    event.traceId()
            ));

            publishResult(event, decision);
            log.info("Order sent for approval event processed id={}, orderId={}",
                    event.eventId(), event.orderId());
        } finally {
            MDC.remove(TraceIdProvider.TRACE_ID);
        }
    }

    private ApprovalDecision approveInStock(OrderSentForApprovalEvent event) {
        if (event.carId() == null) {
            return ApprovalDecision.rejected("Car id is missing");
        }

        return carRepository.findByIdAndRemovedFalse(event.carId())
                .map(this::reserveCar)
                .orElseGet(() -> ApprovalDecision.rejected("Car not found"));
    }

    private ApprovalDecision reserveCar(Car car) {
        if (!car.isAvailable()) {
            return ApprovalDecision.rejected("Car is not available");
        }

        car.markAsUnavailable();
        car.disableTestDrive();
        carRepository.save(car);
        return ApprovalDecision.approve();
    }

    private ApprovalDecision approveCustom(OrderSentForApprovalEvent event, List<UUID> componentOptionIds) {
        if (event.carModelId() == null) {
            return ApprovalDecision.rejected("Car model id is missing");
        }

        if (carModelRepository.findByIdAndRemovedFalse(event.carModelId()).isEmpty()) {
            return ApprovalDecision.rejected("Car model not found");
        }

        if (componentOptionIds.isEmpty()) {
            return ApprovalDecision.rejected("Component options are missing");
        }

        for (UUID componentOptionId : componentOptionIds) {
            ComponentOption componentOption = componentOptionRepository.findByIdAndRemovedFalse(componentOptionId)
                    .orElse(null);
            if (componentOption == null) {
                return ApprovalDecision.rejected("Component option not found: " + componentOptionId);
            }
        }

        return ApprovalDecision.approve();
    }

    private void publishResult(OrderSentForApprovalEvent event, ApprovalDecision decision) {
        OrderApprovalResultEvent resultEvent = new OrderApprovalResultEvent(
                UUID.randomUUID(),
                event.orderId(),
                event.orderType(),
                decision.approved() ? ApprovalResult.APPROVED : ApprovalResult.REJECTED,
                decision.reason(),
                event.traceId(),
                Instant.now()
        );

        outboxEventService.saveEvent(
                event.orderId(),
                "AssemblyOrder",
                OrderApprovalResultEvent.class.getSimpleName(),
                orderApprovalResultTopic,
                resultEvent,
                event.traceId()
        );
        log.info("Saved order approval result to outbox eventId={}, orderId={}, result={}",
                resultEvent.eventId(), resultEvent.orderId(), resultEvent.result());
    }

    private List<UUID> resolveComponentOptionIds(OrderSentForApprovalEvent event) {
        List<UUID> eventComponentOptionIds = safeComponentIds(event.componentOptionIds());
        if (!eventComponentOptionIds.isEmpty() || event.orderType() != OrderType.CUSTOM) {
            return eventComponentOptionIds;
        }

        if (event.configurationId() == null) {
            return List.of();
        }

        return carConfigurationRepository.findByIdAndRemovedFalse(event.configurationId())
                .map(this::extractComponentOptionIds)
                .orElseGet(List::of);
    }

    private List<UUID> extractComponentOptionIds(CarConfiguration configuration) {
        return configuration.getSelectedComponents().values().stream()
                .map(ComponentOption::getId)
                .toList();
    }

    private List<UUID> safeComponentIds(List<UUID> componentOptionIds) {
        return componentOptionIds == null ? List.of() : componentOptionIds;
    }

    private record ApprovalDecision(boolean approved, String reason) {
        private static ApprovalDecision approve() {
            return new ApprovalDecision(true, null);
        }

        private static ApprovalDecision rejected(String reason) {
            return new ApprovalDecision(false, reason);
        }
    }
}

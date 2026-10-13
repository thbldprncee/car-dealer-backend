package io.github.thbldprncee.cardealer.storage.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.thbldprncee.cardealer.events.OrderCanceledEvent;
import io.github.thbldprncee.cardealer.storage.config.TraceIdProvider;
import io.github.thbldprncee.cardealer.storage.domain.model.AssemblyOrder;
import io.github.thbldprncee.cardealer.storage.domain.model.ProcessedEvent;
import io.github.thbldprncee.cardealer.storage.repository.AssemblyOrderRepository;
import io.github.thbldprncee.cardealer.storage.repository.CarRepository;
import io.github.thbldprncee.cardealer.storage.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

/**
 * Compensating side of the approval saga. When an order is canceled after it reached the
 * warehouse, the car reserved for it goes back on sale. If the cancellation arrives before the
 * approval request, a canceled assembly order is recorded so the late request gets rejected.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCanceledListener {

    private final ObjectMapper objectMapper;
    private final ProcessedEventRepository processedEventRepository;
    private final AssemblyOrderRepository assemblyOrderRepository;
    private final CarRepository carRepository;

    @KafkaListener(
            topics = "${app.kafka.topics.order-canceled:order.canceled}",
            groupId = "${spring.kafka.consumer.group-id:storage-service}"
    )
    @Transactional
    public void handle(String payload) throws JsonProcessingException {
        OrderCanceledEvent event = objectMapper.readValue(payload, OrderCanceledEvent.class);
        MDC.put(TraceIdProvider.TRACE_ID, event.traceId());
        try {
            if (processedEventRepository.existsById(event.eventId())) {
                log.info("Skipping duplicate order canceled event id={}", event.eventId());
                return;
            }

            assemblyOrderRepository.findBySourceOrderIdAndRemovedFalse(event.orderId())
                    .ifPresentOrElse(this::cancel, () -> recordEarlyCancellation(event));

            processedEventRepository.save(new ProcessedEvent(
                    event.eventId(),
                    OrderCanceledEvent.class.getSimpleName(),
                    event.traceId()
            ));
        } finally {
            MDC.remove(TraceIdProvider.TRACE_ID);
        }
    }

    private void cancel(AssemblyOrder assemblyOrder) {
        if (assemblyOrder.isCanceled()) {
            return;
        }
        boolean releaseCar = assemblyOrder.cancel();
        assemblyOrderRepository.save(assemblyOrder);

        if (releaseCar) {
            carRepository.findByIdAndRemovedFalse(assemblyOrder.getCarId()).ifPresent(car -> {
                car.markAsAvailable();
                carRepository.save(car);
                log.info("Released car carId={} of canceled order orderId={}",
                        car.getId(), assemblyOrder.getSourceOrderId());
            });
        }
    }

    private void recordEarlyCancellation(OrderCanceledEvent event) {
        AssemblyOrder assemblyOrder = new AssemblyOrder(
                UUID.randomUUID(),
                event.orderId(),
                event.orderType(),
                event.carId(),
                null,
                Set.of(),
                event.traceId()
        );
        assemblyOrder.cancel();
        assemblyOrderRepository.save(assemblyOrder);
        log.info("Order orderId={} canceled before the warehouse saw it", event.orderId());
    }
}

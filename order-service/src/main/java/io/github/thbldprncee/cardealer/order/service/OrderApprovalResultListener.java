package io.github.thbldprncee.cardealer.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.github.thbldprncee.cardealer.order.config.TraceIdProvider;
import io.github.thbldprncee.cardealer.order.domain.exceptions.EntityNotFoundException;
import io.github.thbldprncee.cardealer.order.domain.model.CustomCarOrder;
import io.github.thbldprncee.cardealer.order.domain.model.InStockCarOrder;
import io.github.thbldprncee.cardealer.order.domain.model.ProcessedEvent;
import io.github.thbldprncee.cardealer.events.ApprovalResult;
import io.github.thbldprncee.cardealer.events.OrderApprovalResultEvent;
import io.github.thbldprncee.cardealer.events.OrderType;
import io.github.thbldprncee.cardealer.order.repository.CustomCarOrderRepository;
import io.github.thbldprncee.cardealer.order.repository.InStockCarOrderRepository;
import io.github.thbldprncee.cardealer.order.repository.ProcessedEventRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderApprovalResultListener {

    private final ObjectMapper objectMapper;
    private final ProcessedEventRepository processedEventRepository;
    private final InStockCarOrderRepository inStockCarOrderRepository;
    private final CustomCarOrderRepository customCarOrderRepository;

    @KafkaListener(
            topics = "${app.kafka.topics.order-approval-result:order.approval-result}",
            groupId = "${spring.kafka.consumer.group-id:order-service}"
    )
    @Transactional
    public void handle(String payload) throws JsonProcessingException {
        OrderApprovalResultEvent event = objectMapper.readValue(payload, OrderApprovalResultEvent.class);
        MDC.put(TraceIdProvider.TRACE_ID, event.traceId());
        try {
            log.info("Received order approval result event id={}, orderId={}, orderType={}, result={}, traceId={}",
                    event.eventId(), event.orderId(), event.orderType(), event.result(), event.traceId());
            if (processedEventRepository.existsById(event.eventId())) {
                log.info("Skipping duplicate order approval result event id={}, traceId={}",
                        event.eventId(), event.traceId());
                return;
            }

            if (event.orderType() == OrderType.IN_STOCK) {
                handleInStock(event);
            } else {
                handleCustom(event);
            }

            processedEventRepository.save(new ProcessedEvent(
                    event.eventId(),
                    OrderApprovalResultEvent.class.getSimpleName(),
                    event.traceId()
            ));
            log.info("Order approval result event processed id={}, orderId={}, result={}",
                    event.eventId(), event.orderId(), event.result());
        } finally {
            MDC.remove(TraceIdProvider.TRACE_ID);
        }
    }

    private void handleInStock(OrderApprovalResultEvent event) {
        InStockCarOrder order = inStockCarOrderRepository.findByIdAndRemovedFalse(event.orderId())
                .orElseThrow(() -> new EntityNotFoundException("InStockCarOrder", event.orderId()));

        if (event.result() == ApprovalResult.APPROVED) {
            log.info("Approving in-stock order after warehouse result orderId={}", event.orderId());
            order.markReadyForDeliveryAfterWarehouseApproval();
        } else {
            log.info("Rejecting in-stock order after warehouse result orderId={}, reason={}",
                    event.orderId(), event.reason());
            order.cancelAfterWarehouseRejection(event.reason());
        }
    }

    private void handleCustom(OrderApprovalResultEvent event) {
        CustomCarOrder order = customCarOrderRepository.findByIdAndRemovedFalse(event.orderId())
                .orElseThrow(() -> new EntityNotFoundException("CustomCarOrder", event.orderId()));

        if (event.result() == ApprovalResult.APPROVED) {
            log.info("Approving custom order after warehouse result orderId={}", event.orderId());
            order.approveByWarehouseAfterApprovalResult();
        } else {
            log.info("Rejecting custom order after warehouse result orderId={}, reason={}",
                    event.orderId(), event.reason());
            order.cancelAfterWarehouseRejection(event.reason());
        }
    }
}

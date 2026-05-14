package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.enums.OutboxEventStatus;
import org.example.domain.model.OutboxEvent;
import org.example.repository.OutboxEventRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    @Value("${app.outbox.max-retries:5}")
    private int maxRetries;

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelayString = "${app.outbox.fixed-delay:3000}")
    @Transactional
    public void publishNewEvents() {
        List<OutboxEvent> events = outboxEventRepository
                .findTop50ByStatusAndRemovedFalseOrderByCreatedAtAsc(OutboxEventStatus.NEW);
        log.info("Found {} NEW outbox events", events.size());

        for (OutboxEvent event : events) {
            try {
                log.info("Publishing outbox event id={}, topic={}, aggregateId={}, traceId={}",
                        event.getId(), event.getTopic(), event.getAggregateId(), event.getTraceId());
                kafkaTemplate.send(event.getTopic(), event.getAggregateId().toString(), event.getPayload()).get();
                event.markAsSent();
                log.info("Outbox event published id={}", event.getId());
            } catch (Exception exception) {
                log.error("Failed to publish outbox event id={}, retryCount={}",
                        event.getId(), event.getRetryCount(), exception);
                event.markPublishFailure(exception.getMessage(), maxRetries);
            }
        }
    }
}

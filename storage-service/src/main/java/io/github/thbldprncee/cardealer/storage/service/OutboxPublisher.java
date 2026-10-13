package io.github.thbldprncee.cardealer.storage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.github.thbldprncee.cardealer.storage.domain.model.OutboxEvent;
import io.github.thbldprncee.cardealer.storage.repository.OutboxEventRepository;
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

    private static final int BATCH_SIZE = 50;

    @Value("${app.outbox.max-retries:5}")
    private int maxRetries;

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelayString = "${app.outbox.fixed-delay:3000}")
    @Transactional
    public void publishNewEvents() {
        List<OutboxEvent> events = outboxEventRepository.lockNextBatch(BATCH_SIZE);
        if (!events.isEmpty()) {
            log.info("Publishing {} outbox events", events.size());
        }

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

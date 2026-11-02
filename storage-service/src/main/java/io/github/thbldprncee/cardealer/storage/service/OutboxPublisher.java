package io.github.thbldprncee.cardealer.storage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.github.thbldprncee.cardealer.storage.domain.model.OutboxEvent;
import io.github.thbldprncee.cardealer.storage.repository.OutboxEventRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    @Value("${app.outbox.batch-size:100}")
    private int batchSize;

    @Value("${app.outbox.max-retries:5}")
    private int maxRetries;

    @Value("${app.outbox.send-timeout-ms:10000}")
    private long sendTimeoutMs;

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final TransactionTemplate transactionTemplate;

    /**
     * Publishes batch after batch, each in its own transaction, until a batch comes back short
     * or with a failure. A backlog therefore drains at Kafka's speed instead of one batch per
     * scheduler tick, while a broken broker still gets retried only once per tick.
     */
    @Scheduled(fixedDelayString = "${app.outbox.fixed-delay:200}")
    public void publishNewEvents() {
        Boolean fullBatchSent;
        do {
            fullBatchSent = transactionTemplate.execute(status -> publishNextBatch());
        } while (Boolean.TRUE.equals(fullBatchSent));
    }

    private boolean publishNextBatch() {
        List<OutboxEvent> events = outboxEventRepository.lockNextBatch(batchSize);
        if (events.isEmpty()) {
            return false;
        }
        log.info("Publishing {} outbox events", events.size());

        // Hand the whole batch to the producer first and wait afterwards: the records travel
        // together, and the batch costs one round trip to Kafka instead of one per event.
        List<CompletableFuture<?>> sends = new ArrayList<>(events.size());
        for (OutboxEvent event : events) {
            log.info("Publishing outbox event id={}, topic={}, aggregateId={}, traceId={}",
                    event.getId(), event.getTopic(), event.getAggregateId(), event.getTraceId());
            sends.add(send(event));
        }

        boolean allSent = true;
        for (int i = 0; i < events.size(); i++) {
            OutboxEvent event = events.get(i);
            try {
                sends.get(i).get(sendTimeoutMs, TimeUnit.MILLISECONDS);
                event.markAsSent();
                log.info("Outbox event published id={}", event.getId());
            } catch (Exception exception) {
                if (exception instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                allSent = false;
                log.error("Failed to publish outbox event id={}, retryCount={}",
                        event.getId(), event.getRetryCount(), exception);
                event.markPublishFailure(exception.getMessage(), maxRetries);
            }
        }
        return allSent && events.size() == batchSize;
    }

    private CompletableFuture<?> send(OutboxEvent event) {
        try {
            return kafkaTemplate.send(event.getTopic(), event.getAggregateId().toString(), event.getPayload());
        } catch (Exception exception) {
            return CompletableFuture.failedFuture(exception);
        }
    }
}

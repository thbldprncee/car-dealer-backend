package io.github.thbldprncee.cardealer.order.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import io.github.thbldprncee.cardealer.order.domain.enums.OutboxEventStatus;
import io.github.thbldprncee.cardealer.order.domain.exceptions.DomainValidationException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Getter
@Entity
@Table(name = "outbox_events")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEvent extends BaseEntity {

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    @Column(name = "aggregate_type", nullable = false)
    private String aggregateType;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "payload", nullable = false, columnDefinition = "text")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OutboxEventStatus status;

    @Column(name = "topic", nullable = false)
    private String topic;

    @Column(name = "trace_id", nullable = false)
    private String traceId;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "last_error")
    private String lastError;

    public OutboxEvent(
            UUID id,
            UUID aggregateId,
            String aggregateType,
            String eventType,
            String payload,
            String topic,
            String traceId
    ) {
        this.id = Optional.ofNullable(id)
                .orElseThrow(() -> new DomainValidationException("Outbox event id must not be null"));
        this.aggregateId = Optional.ofNullable(aggregateId)
                .orElseThrow(() -> new DomainValidationException("Aggregate id must not be null"));
        this.aggregateType = validateRequiredText(aggregateType, "Aggregate type must not be blank");
        this.eventType = validateRequiredText(eventType, "Event type must not be blank");
        this.payload = validateRequiredText(payload, "Payload must not be blank");
        this.topic = validateRequiredText(topic, "Topic must not be blank");
        this.traceId = validateRequiredText(traceId, "Trace id must not be blank");
        this.status = OutboxEventStatus.NEW;
    }

    public void markAsSent() {
        this.status = OutboxEventStatus.SENT;
        this.sentAt = Instant.now();
        this.lastError = null;
    }

    public void markPublishFailure(String error, int maxRetries) {
        this.retryCount++;
        this.lastError = error;
        if (retryCount >= maxRetries) {
            this.status = OutboxEventStatus.FAILED;
        }
    }

    private String validateRequiredText(String value, String message) {
        return Optional.ofNullable(value)
                .map(String::trim)
                .filter(text -> !text.isBlank())
                .orElseThrow(() -> new DomainValidationException(message));
    }
}

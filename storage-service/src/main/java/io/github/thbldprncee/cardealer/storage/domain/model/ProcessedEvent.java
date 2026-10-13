package io.github.thbldprncee.cardealer.storage.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Table(name = "processed_events")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProcessedEvent {

    @Id
    private UUID eventId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "trace_id", nullable = false)
    private String traceId;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

    public ProcessedEvent(UUID eventId, String eventType, String traceId) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.traceId = traceId;
        this.processedAt = Instant.now();
    }
}

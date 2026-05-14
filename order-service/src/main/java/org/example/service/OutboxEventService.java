package org.example.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.domain.exceptions.DomainValidationException;
import org.example.domain.model.OutboxEvent;
import org.example.repository.OutboxEventRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OutboxEventService {

    private final ObjectMapper objectMapper;
    private final OutboxEventRepository outboxEventRepository;

    public void saveEvent(
            UUID aggregateId,
            String aggregateType,
            String eventType,
            String topic,
            Object event,
            String traceId
    ) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            outboxEventRepository.save(new OutboxEvent(
                    UUID.randomUUID(),
                    aggregateId,
                    aggregateType,
                    eventType,
                    payload,
                    topic,
                    traceId
            ));
        } catch (JsonProcessingException exception) {
            throw new DomainValidationException("Cannot serialize outbox event: " + exception.getMessage());
        }
    }
}

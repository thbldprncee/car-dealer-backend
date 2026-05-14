package org.example.repository;

import org.example.domain.enums.OutboxEventStatus;
import org.example.domain.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findTop50ByStatusAndRemovedFalseOrderByCreatedAtAsc(OutboxEventStatus status);
}

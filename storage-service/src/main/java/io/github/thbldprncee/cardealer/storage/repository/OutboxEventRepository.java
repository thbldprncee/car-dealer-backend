package io.github.thbldprncee.cardealer.storage.repository;

import io.github.thbldprncee.cardealer.storage.domain.enums.OutboxEventStatus;
import io.github.thbldprncee.cardealer.storage.domain.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findTop50ByStatusAndRemovedFalseOrderByCreatedAtAsc(OutboxEventStatus status);
}

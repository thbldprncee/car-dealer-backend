package io.github.thbldprncee.cardealer.order.repository;

import io.github.thbldprncee.cardealer.order.domain.enums.OutboxEventStatus;
import io.github.thbldprncee.cardealer.order.domain.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findTop50ByStatusAndRemovedFalseOrderByCreatedAtAsc(OutboxEventStatus status);
}

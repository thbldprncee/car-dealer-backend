package io.github.thbldprncee.cardealer.order.repository;

import io.github.thbldprncee.cardealer.order.domain.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    /**
     * Locks the next batch of unsent events. {@code SKIP LOCKED} lets several instances poll the
     * outbox concurrently without publishing the same event twice.
     */
    @Query(value = """
            select * from outbox_events
            where status = 'NEW' and removed = false
            order by created_at
            limit :limit
            for update skip locked
            """, nativeQuery = true)
    List<OutboxEvent> lockNextBatch(@Param("limit") int limit);
}

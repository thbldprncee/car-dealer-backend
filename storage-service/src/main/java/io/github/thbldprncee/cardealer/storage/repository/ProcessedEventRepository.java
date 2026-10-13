package io.github.thbldprncee.cardealer.storage.repository;

import io.github.thbldprncee.cardealer.storage.domain.model.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, UUID> {
}

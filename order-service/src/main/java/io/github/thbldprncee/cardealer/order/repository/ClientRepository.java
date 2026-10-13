package io.github.thbldprncee.cardealer.order.repository;

import io.github.thbldprncee.cardealer.order.domain.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {

    List<Client> findAllByRemovedFalse();

    Optional<Client> findByIdAndRemovedFalse(UUID id);

    Optional<Client> findByEmailIgnoreCaseAndRemovedFalse(String email);
}

package io.github.thbldprncee.cardealer.order.repository;

import io.github.thbldprncee.cardealer.order.domain.enums.TestDriveStatus;
import io.github.thbldprncee.cardealer.order.domain.model.TestDriveRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TestDriveRequestRepository extends JpaRepository<TestDriveRequest, UUID> {

    Optional<TestDriveRequest> findByIdAndRemovedFalse(UUID id);

    List<TestDriveRequest> findAllByRemovedFalse();

    List<TestDriveRequest> findAllByStatusAndRemovedFalse(TestDriveStatus status);

    List<TestDriveRequest> findAllByClientIdAndRemovedFalse(UUID clientId);

    List<TestDriveRequest> findAllByClientIdAndStatusAndRemovedFalse(UUID clientId, TestDriveStatus status);

    List<TestDriveRequest> findAllByCarIdAndRemovedFalse(UUID carId);
}

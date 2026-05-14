package org.example.repository;

import org.example.domain.enums.TestDriveStatus;
import org.example.domain.model.TestDriveRequest;
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

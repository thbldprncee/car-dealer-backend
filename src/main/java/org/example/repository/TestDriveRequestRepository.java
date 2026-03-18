package org.example.repository;

import org.example.domain.model.TestDriveRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TestDriveRequestRepository {

    TestDriveRequest save(TestDriveRequest request);

    Optional<TestDriveRequest> findById(UUID id);

    List<TestDriveRequest> findAll();

    void deleteById(UUID id);

    boolean existsById(UUID id);
}
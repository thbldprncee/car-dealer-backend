package org.example.repository.inmemory;

import org.example.domain.model.TestDriveRequest;
import org.example.repository.TestDriveRequestRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryTestDriveRequestRepository implements TestDriveRequestRepository {

    private final Map<UUID, TestDriveRequest> storage = new ConcurrentHashMap<>();

    @Override
    public TestDriveRequest save(TestDriveRequest request) {
        storage.put(request.getId(), request);
        return request;
    }

    @Override
    public Optional<TestDriveRequest> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<TestDriveRequest> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public void deleteById(UUID id) {
        storage.remove(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return storage.containsKey(id);
    }
}
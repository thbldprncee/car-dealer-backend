package org.example.repository.inmemory;

import org.example.domain.model.SparePart;
import org.example.repository.SparePartRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemorySparePartRepository implements SparePartRepository {

    private final Map<UUID, SparePart> storage = new ConcurrentHashMap<>();

    @Override
    public SparePart save(SparePart sparePart) {
        storage.put(sparePart.id(), sparePart);
        return sparePart;
    }

    @Override
    public Optional<SparePart> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<SparePart> findAll() {
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
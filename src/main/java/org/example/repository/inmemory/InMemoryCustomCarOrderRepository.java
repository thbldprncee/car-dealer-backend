package org.example.repository.inmemory;

import org.example.domain.model.CustomCarOrder;
import org.example.repository.CustomCarOrderRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryCustomCarOrderRepository implements CustomCarOrderRepository {

    private final Map<UUID, CustomCarOrder> storage = new ConcurrentHashMap<>();

    @Override
    public CustomCarOrder save(CustomCarOrder order) {
        storage.put(order.getId(), order);
        return order;
    }

    @Override
    public Optional<CustomCarOrder> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<CustomCarOrder> findAll() {
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
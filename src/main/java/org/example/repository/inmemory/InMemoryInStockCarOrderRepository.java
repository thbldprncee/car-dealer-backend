package org.example.repository.inmemory;

import org.example.domain.model.InStockCarOrder;
import org.example.repository.InStockCarOrderRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryInStockCarOrderRepository implements InStockCarOrderRepository {

    private final Map<UUID, InStockCarOrder> storage = new ConcurrentHashMap<>();

    @Override
    public InStockCarOrder save(InStockCarOrder order) {
        storage.put(order.getId(), order);
        return order;
    }

    @Override
    public Optional<InStockCarOrder> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<InStockCarOrder> findAll() {
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
package org.example.repository.inmemory;

import org.example.domain.model.CarConfiguration;
import org.example.repository.CarConfigurationRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryCarConfigurationRepository implements CarConfigurationRepository {

    private final Map<UUID, CarConfiguration> storage = new ConcurrentHashMap<>();

    @Override
    public CarConfiguration save(CarConfiguration configuration) {
        storage.put(configuration.getId(), configuration);
        return configuration;
    }

    @Override
    public Optional<CarConfiguration> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<CarConfiguration> findAll() {
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
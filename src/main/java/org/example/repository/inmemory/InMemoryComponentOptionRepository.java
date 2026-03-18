package org.example.repository.inmemory;

import org.example.domain.enums.ComponentType;
import org.example.domain.model.ComponentOption;
import org.example.repository.ComponentOptionRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryComponentOptionRepository implements ComponentOptionRepository {

    private final Map<UUID, ComponentOption> storage = new ConcurrentHashMap<>();

    @Override
    public ComponentOption save(ComponentOption componentOption) {
        storage.put(componentOption.id(), componentOption);
        return componentOption;
    }

    @Override
    public Optional<ComponentOption> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<ComponentOption> findAll() {
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

    @Override
    public List<ComponentOption> findByComponentType(ComponentType componentType) {
        return storage.values().stream()
                .filter(option -> option.componentType() == componentType)
                .toList();
    }
}
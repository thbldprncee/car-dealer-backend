package org.example.repository.inmemory;

import org.example.domain.model.CarModel;
import org.example.repository.CarModelRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryCarModelRepository implements CarModelRepository {

    private final Map<UUID, CarModel> storage = new ConcurrentHashMap<>();

    @Override
    public CarModel save(CarModel carModel) {
        storage.put(carModel.getId(), carModel);
        return carModel;
    }

    @Override
    public Optional<CarModel> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<CarModel> findAll() {
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
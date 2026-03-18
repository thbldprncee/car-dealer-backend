package org.example.repository.inmemory;

import org.example.domain.model.Car;
import org.example.repository.CarRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryCarRepository implements CarRepository {

    private final Map<UUID, Car> storage = new ConcurrentHashMap<>();

    @Override
    public Car save(Car car) {
        storage.put(car.getId(), car);
        return car;
    }

    @Override
    public Optional<Car> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Car> findAll() {
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
    public Optional<Car> findByVin(String vin) {
        return storage.values().stream()
                .filter(car -> car.getVin().equalsIgnoreCase(vin))
                .findFirst();
    }

    @Override
    public List<Car> findAvailableCars() {
        return storage.values().stream()
                .filter(Car::isAvailable)
                .toList();
    }

    @Override
    public List<Car> findCarsAvailableForTestDrive() {
        return storage.values().stream()
                .filter(Car::isAvailable)
                .filter(Car::isAvailableForTestDrive)
                .toList();
    }
}
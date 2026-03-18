package org.example.repository.inmemory;

import org.example.domain.model.Client;
import org.example.domain.model.SalesManager;
import org.example.domain.model.SystemAdmin;
import org.example.domain.model.User;
import org.example.domain.model.WarehouseAdmin;
import org.example.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryUserRepository implements UserRepository {

    private final Map<UUID, User> storage = new ConcurrentHashMap<>();

    @Override
    public User save(User user) {
        storage.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<User> findAll() {
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
    public List<Client> findAllClients() {
        return storage.values().stream()
                .filter(Client.class::isInstance)
                .map(Client.class::cast)
                .toList();
    }

    @Override
    public List<SalesManager> findAllSalesManagers() {
        return storage.values().stream()
                .filter(SalesManager.class::isInstance)
                .map(SalesManager.class::cast)
                .toList();
    }

    @Override
    public List<WarehouseAdmin> findAllWarehouseAdmins() {
        return storage.values().stream()
                .filter(WarehouseAdmin.class::isInstance)
                .map(WarehouseAdmin.class::cast)
                .toList();
    }

    @Override
    public List<SystemAdmin> findAllSystemAdmins() {
        return storage.values().stream()
                .filter(SystemAdmin.class::isInstance)
                .map(SystemAdmin.class::cast)
                .toList();
    }
}
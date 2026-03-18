package org.example.repository;

import org.example.domain.model.Client;
import org.example.domain.model.SalesManager;
import org.example.domain.model.SystemAdmin;
import org.example.domain.model.User;
import org.example.domain.model.WarehouseAdmin;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(UUID id);

    List<User> findAll();

    void deleteById(UUID id);

    boolean existsById(UUID id);

    List<Client> findAllClients();

    List<SalesManager> findAllSalesManagers();

    List<WarehouseAdmin> findAllWarehouseAdmins();

    List<SystemAdmin> findAllSystemAdmins();
}
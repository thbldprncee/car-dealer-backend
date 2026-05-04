package org.example.repository;

import org.example.domain.model.SalesManager;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SalesManagerRepository extends JpaRepository<SalesManager, UUID> {

    List<SalesManager> findAllByRemovedFalse();
}
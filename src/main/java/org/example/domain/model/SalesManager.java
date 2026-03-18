package org.example.domain.model;

import java.util.UUID;

public class SalesManager extends User {

    public SalesManager(UUID id, String firstName, String lastName, String email) {
        super(id, firstName, lastName, email);
    }
}
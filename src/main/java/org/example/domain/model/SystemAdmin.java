package org.example.domain.model;

import java.util.UUID;

public class SystemAdmin extends User {

    public SystemAdmin(UUID id, String firstName, String lastName, String email) {
        super(id, firstName, lastName, email);
    }
}
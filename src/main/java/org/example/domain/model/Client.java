package org.example.domain.model;

import java.util.UUID;

public class Client extends User {

    public Client(UUID id, String firstName, String lastName, String email) {
        super(id, firstName, lastName, email);
    }
}
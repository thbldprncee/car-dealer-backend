package org.example.domain.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@DiscriminatorValue("SALES_MANAGER")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SalesManager extends User {

    public SalesManager(UUID id, String firstName, String lastName, String email) {
        super(id, firstName, lastName, email);
    }
}
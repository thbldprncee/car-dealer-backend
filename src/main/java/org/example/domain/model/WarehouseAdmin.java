package org.example.domain.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@DiscriminatorValue("WAREHOUSE_ADMIN")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WarehouseAdmin extends User {

    public WarehouseAdmin(UUID id, String firstName, String lastName, String email) {
        super(id, firstName, lastName, email);
    }
}
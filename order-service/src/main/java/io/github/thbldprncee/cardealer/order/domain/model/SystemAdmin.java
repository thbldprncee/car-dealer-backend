package io.github.thbldprncee.cardealer.order.domain.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@DiscriminatorValue("SYSTEM_ADMIN")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SystemAdmin extends User {

    public SystemAdmin(UUID id, String firstName, String lastName, String email) {
        super(id, firstName, lastName, email);
    }
}
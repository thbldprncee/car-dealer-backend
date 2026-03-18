package org.example.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.example.domain.exceptions.DomainValidationException;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Getter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class User {

    @EqualsAndHashCode.Include
    private final UUID id;
    private final String firstName;
    private final String lastName;
    private final String email;

    protected User(UUID id, String firstName, String lastName, String email) {
        this.id = Optional.ofNullable(id)
                .orElseThrow(() -> new DomainValidationException("User id must not be null"));
        this.firstName = validateRequiredText(firstName, "First name must not be blank");
        this.lastName = validateRequiredText(lastName, "Last name must not be blank");
        this.email = validateRequiredText(email, "Email must not be blank");
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    private String validateRequiredText(String value, String message) {
        return Optional.ofNullable(value)
                .map(String::trim)
                .filter(text -> !text.isBlank())
                .orElseThrow(() -> new DomainValidationException(message));
    }
}
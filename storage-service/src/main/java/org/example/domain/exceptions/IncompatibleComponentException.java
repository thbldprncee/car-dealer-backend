package org.example.domain.exceptions;

public class IncompatibleComponentException extends DomainValidationException {

    public IncompatibleComponentException(String message) {
        super(message);
    }
}
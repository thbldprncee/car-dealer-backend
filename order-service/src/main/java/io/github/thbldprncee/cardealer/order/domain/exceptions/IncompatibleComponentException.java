package io.github.thbldprncee.cardealer.order.domain.exceptions;

public class IncompatibleComponentException extends DomainValidationException {

    public IncompatibleComponentException(String message) {
        super(message);
    }
}
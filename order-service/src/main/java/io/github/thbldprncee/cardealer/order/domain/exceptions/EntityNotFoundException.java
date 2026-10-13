package io.github.thbldprncee.cardealer.order.domain.exceptions;

public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String message) {
        super(message);
    }

    public EntityNotFoundException(String entityName, Object id) {
        super(String.format("%s with id=%s was not found", entityName, id));
    }
}
package io.github.thbldprncee.cardealer.order.domain.exceptions;

public class GrpcServiceUnavailableException extends RuntimeException {

    public GrpcServiceUnavailableException(String message) {
        super(message);
    }

    public GrpcServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}

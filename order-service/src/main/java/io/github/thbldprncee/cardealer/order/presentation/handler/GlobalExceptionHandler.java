package io.github.thbldprncee.cardealer.order.presentation.handler;

import io.github.thbldprncee.cardealer.order.domain.exceptions.DomainValidationException;
import io.github.thbldprncee.cardealer.order.domain.exceptions.EntityNotFoundException;
import io.github.thbldprncee.cardealer.order.domain.exceptions.GrpcServiceUnavailableException;
import io.github.thbldprncee.cardealer.order.domain.exceptions.IncompatibleComponentException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleEntityNotFoundException(EntityNotFoundException exception) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of(
                        HttpStatus.NOT_FOUND,
                        exception.getMessage()
                ));
    }

    @ExceptionHandler(DomainValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleDomainValidationException(DomainValidationException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of(
                        HttpStatus.BAD_REQUEST,
                        exception.getMessage()
                ));
    }

    @ExceptionHandler(IncompatibleComponentException.class)
    public ResponseEntity<ApiErrorResponse> handleIncompatibleComponentException(
            IncompatibleComponentException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of(
                        HttpStatus.BAD_REQUEST,
                        exception.getMessage()
                ));
    }

    @ExceptionHandler(GrpcServiceUnavailableException.class)
    public ResponseEntity<ApiErrorResponse> handleGrpcServiceUnavailableException(
            GrpcServiceUnavailableException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiErrorResponse.of(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        exception.getMessage()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception
    ) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> "%s: %s".formatted(error.getField(), error.getDefaultMessage()))
                .orElse("Request validation failed");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of(
                        HttpStatus.BAD_REQUEST,
                        message
                ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDeniedException(AccessDeniedException exception) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiErrorResponse.of(
                        HttpStatus.FORBIDDEN,
                        "Access denied"
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(Exception exception) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.of(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Unexpected server error"
                ));
    }

    public record ApiErrorResponse(
            Instant timestamp,
            int status,
            String error,
            String message
    ) {

        public static ApiErrorResponse of(HttpStatus status, String message) {
            return new ApiErrorResponse(
                    Instant.now(),
                    status.value(),
                    status.getReasonPhrase(),
                    message
            );
        }
    }
}

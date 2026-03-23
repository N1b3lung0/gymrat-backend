package com.n1b3lung0.gymrat.domain.exception;

/**
 * Thrown when an operation would create a duplicate or conflicting state.
 *
 * <p>Maps to HTTP 409 (Conflict) at the REST layer.
 */
public abstract class ConflictException extends DomainException {

    protected ConflictException(String message) {
        super(message);
    }

    protected ConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}


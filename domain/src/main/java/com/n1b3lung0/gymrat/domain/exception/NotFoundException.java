package com.n1b3lung0.gymrat.domain.exception;

/**
 * Thrown when a requested aggregate or entity cannot be found.
 *
 * <p>Maps to HTTP 404 at the REST layer.
 */
public abstract class NotFoundException extends DomainException {

    protected NotFoundException(String message) {
        super(message);
    }

    protected NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}


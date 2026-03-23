package com.n1b3lung0.gymrat.domain.exception;

/**
 * Thrown when an operation violates a domain business rule or invariant.
 *
 * <p>Maps to HTTP 422 (Unprocessable Entity) at the REST layer.
 */
public abstract class BusinessRuleViolationException extends DomainException {

    protected BusinessRuleViolationException(String message) {
        super(message);
    }

    protected BusinessRuleViolationException(String message, Throwable cause) {
        super(message, cause);
    }
}


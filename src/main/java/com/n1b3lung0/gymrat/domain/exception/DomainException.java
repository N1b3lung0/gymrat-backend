package com.n1b3lung0.gymrat.domain.exception;

/**
 * Base class for all domain exceptions.
 *
 * <p>Extends {@link RuntimeException} so callers are not forced to declare
 * checked exceptions — domain rules violations are programming/business errors,
 * not recoverable conditions that every caller should handle explicitly.
 *
 * <p>All domain-specific exceptions must extend one of the concrete subclasses
 * ({@link NotFoundException}, {@link BusinessRuleViolationException},
 * {@link ConflictException}) rather than this class directly.
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }

    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}


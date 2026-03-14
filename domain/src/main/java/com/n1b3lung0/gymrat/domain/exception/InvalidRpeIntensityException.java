package com.n1b3lung0.gymrat.domain.exception;

/**
 * Thrown when a {@code Series} intensity value falls outside the valid RPE scale (1–10).
 */
public class InvalidRpeIntensityException extends BusinessRuleViolationException {

    public InvalidRpeIntensityException(int intensity) {
        super("RPE intensity must be between 1 and 10 (inclusive), but got: " + intensity);
    }
}


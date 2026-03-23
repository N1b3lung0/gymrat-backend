package com.n1b3lung0.gymrat.domain.exception;

/**
 * Thrown when an {@code Exercise} with the same name already exists.
 */
public class DuplicateExerciseNameException extends ConflictException {

    public DuplicateExerciseNameException(String name) {
        super("An exercise with name '" + name + "' already exists");
    }
}


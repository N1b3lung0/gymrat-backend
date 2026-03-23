package com.n1b3lung0.gymrat.domain.exception;

import com.n1b3lung0.gymrat.domain.model.ExerciseId;

/**
 * Thrown when an {@code Exercise} cannot be found by its identifier.
 */
public class ExerciseNotFoundException extends NotFoundException {

    public ExerciseNotFoundException(ExerciseId id) {
        super("Exercise not found with id: " + id);
    }
}


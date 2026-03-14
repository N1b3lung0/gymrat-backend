package com.n1b3lung0.gymrat.domain.exception;

import com.n1b3lung0.gymrat.domain.model.WorkoutId;

/**
 * Thrown when a {@code Workout} cannot be found by its identifier.
 */
public class WorkoutNotFoundException extends NotFoundException {

    public WorkoutNotFoundException(WorkoutId id) {
        super("Workout not found with id: " + id);
    }
}


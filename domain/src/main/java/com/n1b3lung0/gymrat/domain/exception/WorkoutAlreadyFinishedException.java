package com.n1b3lung0.gymrat.domain.exception;

import com.n1b3lung0.gymrat.domain.model.WorkoutId;

/**
 * Thrown when an operation is attempted on a {@code Workout} that has already been finished.
 */
public class WorkoutAlreadyFinishedException extends BusinessRuleViolationException {

    public WorkoutAlreadyFinishedException(WorkoutId id) {
        super("Workout " + id + " is already finished and cannot be modified");
    }
}


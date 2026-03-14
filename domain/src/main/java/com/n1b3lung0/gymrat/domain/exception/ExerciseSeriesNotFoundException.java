package com.n1b3lung0.gymrat.domain.exception;

import com.n1b3lung0.gymrat.domain.model.ExerciseSeriesId;

/**
 * Thrown when an {@code ExerciseSeries} cannot be found by its identifier.
 */
public class ExerciseSeriesNotFoundException extends NotFoundException {

    public ExerciseSeriesNotFoundException(ExerciseSeriesId id) {
        super("ExerciseSeries not found with id: " + id);
    }
}


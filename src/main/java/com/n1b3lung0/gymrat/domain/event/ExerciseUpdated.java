package com.n1b3lung0.gymrat.domain.event;

import com.n1b3lung0.gymrat.domain.model.ExerciseId;

import java.time.Instant;

/**
 * Emitted when an existing {@code Exercise} is updated.
 *
 * @param exerciseId  identifier of the updated exercise
 * @param occurredOn  timestamp when the event occurred
 */
public record ExerciseUpdated(ExerciseId exerciseId, Instant occurredOn)
        implements ExerciseEvent {

    public ExerciseUpdated(ExerciseId exerciseId) {
        this(exerciseId, Instant.now());
    }
}


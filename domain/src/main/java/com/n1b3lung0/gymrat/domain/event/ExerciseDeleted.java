package com.n1b3lung0.gymrat.domain.event;

import com.n1b3lung0.gymrat.domain.model.ExerciseId;

import java.time.Instant;

/**
 * Emitted when an {@code Exercise} is soft-deleted.
 *
 * @param exerciseId  identifier of the deleted exercise
 * @param occurredOn  timestamp when the event occurred
 */
public record ExerciseDeleted(ExerciseId exerciseId, Instant occurredOn)
        implements ExerciseEvent {

    public ExerciseDeleted(ExerciseId exerciseId) {
        this(exerciseId, Instant.now());
    }
}


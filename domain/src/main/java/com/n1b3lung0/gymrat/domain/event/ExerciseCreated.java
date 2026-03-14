package com.n1b3lung0.gymrat.domain.event;

import com.n1b3lung0.gymrat.domain.model.ExerciseId;

import java.time.Instant;

/**
 * Emitted when a new {@code Exercise} is created.
 *
 * @param exerciseId  identifier of the created exercise
 * @param name        name of the created exercise
 * @param occurredOn  timestamp when the event occurred
 */
public record ExerciseCreated(ExerciseId exerciseId, String name, Instant occurredOn)
        implements ExerciseEvent {

    public ExerciseCreated(ExerciseId exerciseId, String name) {
        this(exerciseId, name, Instant.now());
    }
}


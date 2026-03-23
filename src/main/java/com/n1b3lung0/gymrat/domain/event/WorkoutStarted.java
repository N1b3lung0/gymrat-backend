package com.n1b3lung0.gymrat.domain.event;

import com.n1b3lung0.gymrat.domain.model.WorkoutId;

import java.time.Instant;

/**
 * Emitted when a new {@code Workout} session is started.
 *
 * @param workoutId    identifier of the started workout
 * @param startWorkout timestamp when the workout began
 * @param occurredOn   timestamp when the event occurred
 */
public record WorkoutStarted(WorkoutId workoutId, Instant startWorkout, Instant occurredOn)
        implements WorkoutEvent {

    public WorkoutStarted(WorkoutId workoutId, Instant startWorkout) {
        this(workoutId, startWorkout, Instant.now());
    }
}


package com.n1b3lung0.gymrat.domain.event;

import com.n1b3lung0.gymrat.domain.model.WorkoutId;

import java.time.Instant;

/**
 * Emitted when a {@code Workout} session is finished.
 *
 * @param workoutId  identifier of the finished workout
 * @param endWorkout timestamp when the workout ended
 * @param occurredOn timestamp when the event occurred
 */
public record WorkoutFinished(WorkoutId workoutId, Instant endWorkout, Instant occurredOn)
        implements WorkoutEvent {

    public WorkoutFinished(WorkoutId workoutId, Instant endWorkout) {
        this(workoutId, endWorkout, Instant.now());
    }
}


package com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * REST response DTO carrying the full detail of a workout.
 *
 * <p>Returned by {@code GET /api/v1/workouts/{id}} and {@code PATCH /api/v1/workouts/{id}/finish}.
 *
 * @param id                workout UUID
 * @param startWorkout      timestamp when the session started
 * @param endWorkout        timestamp when the session ended; {@code null} if still open
 * @param finished          {@code true} if the workout has been finished
 * @param exerciseSeriesIds UUIDs of the exercise sessions in this workout
 */
public record WorkoutResponse(
        UUID id,
        Instant startWorkout,
        Instant endWorkout,
        boolean finished,
        List<UUID> exerciseSeriesIds
) {}


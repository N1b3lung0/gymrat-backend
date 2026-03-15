package com.n1b3lung0.gymrat.application.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Full detail view for a single {@code Workout}.
 *
 * @param id                 workout UUID
 * @param startWorkout       timestamp when the session started
 * @param endWorkout         timestamp when the session ended; {@code null} if still open
 * @param exerciseSeriesIds  IDs of the exercise sessions in this workout
 */
public record WorkoutDetailView(
        UUID id,
        Instant startWorkout,
        Instant endWorkout,
        List<UUID> exerciseSeriesIds
) {}


package com.n1b3lung0.gymrat.application.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Lightweight summary view for paginated {@code Workout} listings.
 *
 * @param id           workout UUID
 * @param startWorkout timestamp when the session started
 * @param endWorkout   timestamp when the session ended; {@code null} if still open
 * @param finished     {@code true} if the workout has been finished
 */
public record WorkoutSummaryView(
        UUID id,
        Instant startWorkout,
        Instant endWorkout,
        boolean finished
) {}


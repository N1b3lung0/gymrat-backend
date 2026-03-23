package com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Lightweight REST response DTO for paginated workout listings.
 *
 * <p>Omits {@code exerciseSeriesIds} to keep list responses lean.
 * Use {@link WorkoutResponse} for full detail.
 *
 * @param id           workout UUID
 * @param startWorkout timestamp when the session started
 * @param endWorkout   timestamp when the session ended; {@code null} if still open
 * @param finished     {@code true} if the workout has been finished
 */
public record WorkoutSummaryResponse(
        UUID id,
        Instant startWorkout,
        Instant endWorkout,
        boolean finished
) {}


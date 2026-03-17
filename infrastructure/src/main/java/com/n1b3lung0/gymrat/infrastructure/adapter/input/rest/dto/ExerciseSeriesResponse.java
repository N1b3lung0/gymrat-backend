package com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto;

import java.util.List;
import java.util.UUID;

/**
 * REST response DTO carrying the full detail of an ExerciseSeries.
 *
 * <p>Returned by {@code POST} and {@code GET /workouts/{wId}/exercises/{esId}}.
 *
 * @param id         exercise-series UUID
 * @param workoutId  parent workout UUID
 * @param exerciseId exercise UUID
 * @param seriesIds  UUIDs of the series within this exercise session
 */
public record ExerciseSeriesResponse(
        UUID id,
        UUID workoutId,
        UUID exerciseId,
        List<UUID> seriesIds
) {}


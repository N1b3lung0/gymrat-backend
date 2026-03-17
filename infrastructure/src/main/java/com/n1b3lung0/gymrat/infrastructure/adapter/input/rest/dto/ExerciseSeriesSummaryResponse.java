package com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto;

import java.util.UUID;

/**
 * Lightweight REST response DTO for ExerciseSeries listings within a workout.
 *
 * <p>Omits {@code seriesIds} to keep list responses lean.
 * Use {@link ExerciseSeriesResponse} for full detail.
 *
 * @param id          exercise-series UUID
 * @param exerciseId  exercise UUID
 * @param seriesCount number of series recorded in this exercise session
 */
public record ExerciseSeriesSummaryResponse(
        UUID id,
        UUID exerciseId,
        int seriesCount
) {}


package com.n1b3lung0.gymrat.application.dto;

import java.util.UUID;

/**
 * Lightweight summary view for {@code ExerciseSeries} listings within a workout.
 *
 * @param id          exercise-series UUID
 * @param exerciseId  exercise UUID
 * @param seriesCount number of series recorded in this exercise session
 */
public record ExerciseSeriesSummaryView(
        UUID id,
        UUID exerciseId,
        int seriesCount
) {}


package com.n1b3lung0.gymrat.application.dto;

import java.util.List;
import java.util.UUID;

/**
 * Full detail view for a single {@code ExerciseSeries}.
 *
 * @param id         exercise-series UUID
 * @param workoutId  parent workout UUID
 * @param exerciseId exercise UUID
 * @param seriesIds  UDs of the series within this exercise session
 */
public record ExerciseSeriesDetailView(
        UUID id,
        UUID workoutId,
        UUID exerciseId,
        List<UUID> seriesIds
) {}


package com.n1b3lung0.gymrat.application.dto;

import com.n1b3lung0.gymrat.domain.model.ExerciseSeriesId;

/**
 * Query to retrieve the full detail of a single {@code ExerciseSeries}.
 *
 * @param id the exercise-series identifier
 */
public record GetExerciseSeriesByIdQuery(ExerciseSeriesId id) {}


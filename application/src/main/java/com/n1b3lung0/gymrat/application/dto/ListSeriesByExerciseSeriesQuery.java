package com.n1b3lung0.gymrat.application.dto;

import com.n1b3lung0.gymrat.domain.model.ExerciseSeriesId;

/**
 * Query to list all series sets within an exercise-series.
 *
 * @param exerciseSeriesId the parent exercise-series identifier
 */
public record ListSeriesByExerciseSeriesQuery(ExerciseSeriesId exerciseSeriesId) {}


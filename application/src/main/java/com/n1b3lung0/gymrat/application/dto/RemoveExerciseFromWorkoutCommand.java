package com.n1b3lung0.gymrat.application.dto;

import com.n1b3lung0.gymrat.domain.model.ExerciseSeriesId;

/**
 * Command to remove an exercise session from a workout (soft-delete the {@code ExerciseSeries}).
 *
 * @param id the exercise-series identifier to remove
 */
public record RemoveExerciseFromWorkoutCommand(ExerciseSeriesId id) {}


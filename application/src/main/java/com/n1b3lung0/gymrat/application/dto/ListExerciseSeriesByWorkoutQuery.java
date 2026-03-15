package com.n1b3lung0.gymrat.application.dto;

import com.n1b3lung0.gymrat.domain.model.WorkoutId;

/**
 * Query to list all {@code ExerciseSeries} belonging to a workout.
 *
 * @param workoutId the parent workout identifier
 */
public record ListExerciseSeriesByWorkoutQuery(WorkoutId workoutId) {}


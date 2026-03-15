package com.n1b3lung0.gymrat.application.dto;

import com.n1b3lung0.gymrat.domain.model.ExerciseId;
import com.n1b3lung0.gymrat.domain.model.WorkoutId;

/**
 * Command to add an exercise to a workout, creating a new {@code ExerciseSeries}.
 *
 * @param workoutId  the target workout
 * @param exerciseId the exercise being added
 */
public record AddExerciseToWorkoutCommand(WorkoutId workoutId, ExerciseId exerciseId) {}


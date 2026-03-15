package com.n1b3lung0.gymrat.application.dto;

import com.n1b3lung0.gymrat.domain.model.ExerciseId;

/**
 * Command to soft-delete an existing exercise.
 *
 * @param id identifier of the exercise to delete
 */
public record DeleteExerciseCommand(ExerciseId id) {}


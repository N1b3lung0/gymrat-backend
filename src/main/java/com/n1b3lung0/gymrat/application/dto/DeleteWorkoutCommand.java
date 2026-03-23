package com.n1b3lung0.gymrat.application.dto;

import com.n1b3lung0.gymrat.domain.model.WorkoutId;

/**
 * Command to soft-delete a workout.
 *
 * @param id identifier of the workout to delete
 */
public record DeleteWorkoutCommand(WorkoutId id) {}


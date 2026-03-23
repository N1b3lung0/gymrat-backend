package com.n1b3lung0.gymrat.application.dto;

import com.n1b3lung0.gymrat.domain.model.WorkoutId;

import java.time.Instant;

/**
 * Command to finish an open workout session.
 *
 * @param id         identifier of the workout to finish
 * @param endWorkout timestamp when the session ends; must be after startWorkout
 */
public record FinishWorkoutCommand(WorkoutId id, Instant endWorkout) {}


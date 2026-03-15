package com.n1b3lung0.gymrat.application.dto;

import com.n1b3lung0.gymrat.domain.model.WorkoutId;

/**
 * Query to retrieve the full detail of a single workout.
 *
 * @param id identifier of the workout to retrieve
 */
public record GetWorkoutByIdQuery(WorkoutId id) {}


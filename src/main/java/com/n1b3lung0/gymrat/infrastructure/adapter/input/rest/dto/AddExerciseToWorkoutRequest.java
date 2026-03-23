package com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * REST request DTO for adding an exercise to a workout (creates an ExerciseSeries).
 *
 * @param exerciseId the exercise UUID to add; must not be null
 */
public record AddExerciseToWorkoutRequest(
        @NotNull(message = "exerciseId must not be null")
        UUID exerciseId
) {}


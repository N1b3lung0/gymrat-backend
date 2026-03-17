package com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/**
 * REST request DTO for finishing an open workout session.
 *
 * @param endWorkout timestamp when the session ends; must not be null and must be after startWorkout
 */
public record FinishWorkoutRequest(
        @NotNull(message = "endWorkout must not be null")
        Instant endWorkout
) {}


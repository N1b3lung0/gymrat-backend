package com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/**
 * REST request DTO for starting a new workout session.
 *
 * @param startWorkout timestamp when the session begins; must not be null
 */
public record CreateWorkoutRequest(
        @NotNull(message = "startWorkout must not be null")
        Instant startWorkout
) {}


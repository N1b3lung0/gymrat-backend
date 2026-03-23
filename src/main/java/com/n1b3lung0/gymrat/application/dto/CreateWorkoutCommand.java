package com.n1b3lung0.gymrat.application.dto;

import java.time.Instant;

/**
 * Command to start a new workout session.
 *
 * @param startWorkout timestamp when the session begins; must not be null
 */
public record CreateWorkoutCommand(Instant startWorkout) {}


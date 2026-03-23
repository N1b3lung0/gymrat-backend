package com.n1b3lung0.gymrat.domain.event;

/**
 * Sealed interface for all domain events related to the {@code Workout} aggregate.
 *
 * <p>Use pattern matching ({@code switch} expressions) to handle variants exhaustively.
 */
public sealed interface WorkoutEvent permits WorkoutStarted, WorkoutFinished {
}


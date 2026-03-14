package com.n1b3lung0.gymrat.domain.event;

/**
 * Sealed interface for all domain events related to the {@code Exercise} aggregate.
 *
 * <p>Use pattern matching ({@code switch} expressions) to handle variants exhaustively.
 */
public sealed interface ExerciseEvent permits ExerciseCreated, ExerciseUpdated, ExerciseDeleted {
}


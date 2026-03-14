package com.n1b3lung0.gymrat.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object that represents the unique identity of a {@link Workout}.
 *
 * <p>Use {@link #generate()} to create a new identity and {@link #of(UUID)} to
 * reconstitute an existing one from persistence.
 */
public record WorkoutId(UUID value) {

    /** Compact constructor — guards against null values. */
    public WorkoutId {
        Objects.requireNonNull(value, "WorkoutId value must not be null");
    }

    /**
     * Creates a brand-new {@code WorkoutId} backed by a random UUID.
     *
     * @return a new {@code WorkoutId}
     */
    public static WorkoutId generate() {
        return new WorkoutId(UUID.randomUUID());
    }

    /**
     * Reconstitutes a {@code WorkoutId} from an existing {@link UUID}.
     *
     * @param value the UUID sourced from persistence or an external system
     * @return the corresponding {@code WorkoutId}
     */
    public static WorkoutId of(UUID value) {
        return new WorkoutId(value);
    }

    /**
     * Convenience factory that parses a UUID string.
     *
     * @param value the UUID string representation
     * @return the corresponding {@code WorkoutId}
     * @throws IllegalArgumentException if the string is not a valid UUID
     */
    public static WorkoutId of(String value) {
        Objects.requireNonNull(value, "WorkoutId string value must not be null");
        return new WorkoutId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}


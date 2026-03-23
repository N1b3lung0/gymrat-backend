package com.n1b3lung0.gymrat.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object that represents the unique identity of an {@link Exercise}.
 *
 * <p>Use {@link #generate()} to create a new identity and {@link #of(UUID)} to
 * reconstitute an existing one from persistence.
 */
public record ExerciseId(UUID value) {

    /** Compact constructor — guards against null values. */
    public ExerciseId {
        Objects.requireNonNull(value, "ExerciseId value must not be null");
    }

    /**
     * Creates a brand-new {@code ExerciseId} backed by a random UUID.
     *
     * @return a new {@code ExerciseId}
     */
    public static ExerciseId generate() {
        return new ExerciseId(UUID.randomUUID());
    }

    /**
     * Reconstitutes an {@code ExerciseId} from an existing {@link UUID}.
     *
     * @param value the UUID sourced from persistence or an external system
     * @return the corresponding {@code ExerciseId}
     */
    public static ExerciseId of(UUID value) {
        return new ExerciseId(value);
    }

    /**
     * Convenience factory that parses a UUID string.
     *
     * @param value the UUID string representation
     * @return the corresponding {@code ExerciseId}
     * @throws IllegalArgumentException if the string is not a valid UUID
     */
    public static ExerciseId of(String value) {
        Objects.requireNonNull(value, "ExerciseId string value must not be null");
        return new ExerciseId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}


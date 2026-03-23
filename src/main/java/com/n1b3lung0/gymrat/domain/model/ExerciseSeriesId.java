package com.n1b3lung0.gymrat.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object that represents the unique identity of an {@link ExerciseSeries}.
 *
 * <p>Use {@link #generate()} to create a new identity and {@link #of(UUID)} to
 * reconstitute an existing one from persistence.
 */
public record ExerciseSeriesId(UUID value) {

    /** Compact constructor — guards against null values. */
    public ExerciseSeriesId {
        Objects.requireNonNull(value, "ExerciseSeriesId value must not be null");
    }

    /**
     * Creates a brand-new {@code ExerciseSeriesId} backed by a random UUID.
     *
     * @return a new {@code ExerciseSeriesId}
     */
    public static ExerciseSeriesId generate() {
        return new ExerciseSeriesId(UUID.randomUUID());
    }

    /**
     * Reconstitutes an {@code ExerciseSeriesId} from an existing {@link UUID}.
     *
     * @param value the UUID sourced from persistence or an external system
     * @return the corresponding {@code ExerciseSeriesId}
     */
    public static ExerciseSeriesId of(UUID value) {
        return new ExerciseSeriesId(value);
    }

    /**
     * Convenience factory that parses a UUID string.
     *
     * @param value the UUID string representation
     * @return the corresponding {@code ExerciseSeriesId}
     * @throws IllegalArgumentException if the string is not a valid UUID
     */
    public static ExerciseSeriesId of(String value) {
        Objects.requireNonNull(value, "ExerciseSeriesId string value must not be null");
        return new ExerciseSeriesId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}


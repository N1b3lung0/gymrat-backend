package com.n1b3lung0.gymrat.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object that represents the unique identity of a {@link Series}.
 *
 * <p>Use {@link #generate()} to create a new identity and {@link #of(UUID)} to
 * reconstitute an existing one from persistence.
 */
public record SeriesId(UUID value) {

    /** Compact constructor — guards against null values. */
    public SeriesId {
        Objects.requireNonNull(value, "SeriesId value must not be null");
    }

    /**
     * Creates a brand-new {@code SeriesId} backed by a random UUID.
     *
     * @return a new {@code SeriesId}
     */
    public static SeriesId generate() {
        return new SeriesId(UUID.randomUUID());
    }

    /**
     * Reconstitutes a {@code SeriesId} from an existing {@link UUID}.
     *
     * @param value the UUID sourced from persistence or an external system
     * @return the corresponding {@code SeriesId}
     */
    public static SeriesId of(UUID value) {
        return new SeriesId(value);
    }

    /**
     * Convenience factory that parses a UUID string.
     *
     * @param value the UUID string representation
     * @return the corresponding {@code SeriesId}
     * @throws IllegalArgumentException if the string is not a valid UUID
     */
    public static SeriesId of(String value) {
        Objects.requireNonNull(value, "SeriesId string value must not be null");
        return new SeriesId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}


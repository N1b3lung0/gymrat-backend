package com.n1b3lung0.gymrat.domain.model;

import java.util.Objects;

/**
 * Value Object representing a media asset (image or video) associated with an {@link Exercise}.
 *
 * <p>Stored in a dedicated {@code media} table to avoid duplication when multiple exercises
 * reference the same asset. The domain model stays clean — deduplication logic lives in the
 * persistence adapter.
 *
 * @param name        human-readable name of the asset
 * @param description optional description of what the asset shows
 * @param url         publicly accessible URL of the asset; must not be blank
 */
public record Media(String name, String description, String url) {

    /** Compact constructor — validates required fields. */
    public Media {
        Objects.requireNonNull(url, "Media url must not be null");
        if (url.isBlank()) {
            throw new IllegalArgumentException("Media url must not be blank");
        }
    }

    /**
     * Factory method for creating a fully described media asset.
     *
     * @param name        human-readable name
     * @param description optional description
     * @param url         asset URL
     * @return a new {@code Media} instance
     */
    public static Media of(String name, String description, String url) {
        return new Media(name, description, url);
    }

    /**
     * Factory method for creating a media asset with URL only.
     *
     * @param url asset URL
     * @return a new {@code Media} instance with {@code null} name and description
     */
    public static Media of(String url) {
        return new Media(null, null, url);
    }
}


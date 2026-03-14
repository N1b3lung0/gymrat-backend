package com.n1b3lung0.gymrat.domain.repository;

import com.n1b3lung0.gymrat.domain.model.Media;

import java.util.Optional;
import java.util.UUID;

/**
 * Output port — persistence contract for {@link Media} assets.
 *
 * <p>Media assets (images and videos) are stored in a dedicated table to avoid
 * duplication when multiple exercises reference the same URL. The adapter is
 * responsible for deduplication logic.
 */
public interface MediaRepositoryPort {

    /**
     * Persists a new or updated {@link Media} asset.
     *
     * @param media the asset to save
     * @return the saved asset (may carry a generated identifier from persistence)
     */
    Media save(Media media);

    /**
     * Returns the {@link Media} asset with the given identifier, if it exists.
     *
     * @param id the media identifier from persistence
     * @return an {@link Optional} containing the asset, or empty if not found
     */
    Optional<Media> findById(UUID id);

    /**
     * Returns the {@link Media} asset with the given URL, if it exists.
     *
     * <p>Used by the adapter to detect duplicate assets before persisting a new one.
     *
     * @param url the asset URL
     * @return an {@link Optional} containing the existing asset, or empty if not found
     */
    Optional<Media> findByUrl(String url);
}


package com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence.repository;

import com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence.entity.MediaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link MediaEntity}.
 */
public interface SpringMediaRepository extends JpaRepository<MediaEntity, UUID> {

    /**
     * Returns the active media asset with the given URL, if it exists.
     * Used for deduplication before persisting a new asset.
     */
    Optional<MediaEntity> findByUrl(String url);
}


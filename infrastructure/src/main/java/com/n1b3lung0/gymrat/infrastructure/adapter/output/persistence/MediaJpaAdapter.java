package com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence;

import com.n1b3lung0.gymrat.domain.model.AuditFields;
import com.n1b3lung0.gymrat.domain.model.Media;
import com.n1b3lung0.gymrat.domain.repository.MediaRepositoryPort;
import com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence.entity.AuditEmbeddable;
import com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence.entity.MediaEntity;
import com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence.repository.SpringMediaRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA adapter that implements {@link MediaRepositoryPort}.
 *
 * <p>Handles deduplication: if a {@link Media} asset with the same URL already exists
 * in the database, the existing record is returned instead of creating a duplicate.
 */
@Component
public class MediaJpaAdapter implements MediaRepositoryPort {

    private final SpringMediaRepository springMediaRepository;

    public MediaJpaAdapter(SpringMediaRepository springMediaRepository) {
        this.springMediaRepository = springMediaRepository;
    }

    // -------------------------------------------------------------------------
    // MediaRepositoryPort
    // -------------------------------------------------------------------------

    @Override
    public Media save(Media media) {
        // Deduplication: reuse existing asset if the URL already exists
        return springMediaRepository.findByUrl(media.url())
                .map(this::toDomain)
                .orElseGet(() -> {
                    var entity = toEntity(media);
                    return toDomain(springMediaRepository.save(entity));
                });
    }

    @Override
    public Optional<Media> findById(UUID id) {
        return springMediaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Media> findByUrl(String url) {
        return springMediaRepository.findByUrl(url).map(this::toDomain);
    }

    // -------------------------------------------------------------------------
    // Mapping helpers (Media is a simple value object — no dedicated mapper class)
    // -------------------------------------------------------------------------

    private MediaEntity toEntity(Media media) {
        var now = Instant.now();
        return MediaEntity.builder()
                .id(UUID.randomUUID())
                .name(media.name())
                .description(media.description())
                .url(media.url())
                .audit(AuditEmbeddable.fromDomain(
                        new AuditFields(now, "system", null, null, true, null, null)))
                .build();
    }

    private Media toDomain(MediaEntity entity) {
        return Media.of(entity.getName(), entity.getDescription(), entity.getUrl());
    }
}


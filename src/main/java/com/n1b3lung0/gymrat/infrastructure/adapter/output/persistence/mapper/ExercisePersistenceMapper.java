package com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence.mapper;

import com.n1b3lung0.gymrat.application.dto.ExerciseDetailView;
import com.n1b3lung0.gymrat.application.dto.ExerciseSummaryView;
import com.n1b3lung0.gymrat.application.dto.MediaView;
import com.n1b3lung0.gymrat.domain.model.AuditFields;
import com.n1b3lung0.gymrat.domain.model.Exercise;
import com.n1b3lung0.gymrat.domain.model.ExerciseId;
import com.n1b3lung0.gymrat.domain.model.ExerciseSeriesId;
import com.n1b3lung0.gymrat.domain.model.Media;
import com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence.entity.AuditEmbeddable;
import com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence.entity.ExerciseEntity;
import com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence.entity.ExerciseSeriesEntity;
import com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence.entity.MediaEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Hand-written mapper between the {@link Exercise} domain aggregate and
 * the {@link ExerciseEntity} JPA entity / application read-model views.
 *
 * <p>No MapStruct or reflection — explicit field-by-field mapping keeps the
 * infrastructure boundary clear and compile-time safe.
 */
@Component
public class ExercisePersistenceMapper {

    // -------------------------------------------------------------------------
    // Domain → Entity
    // -------------------------------------------------------------------------

    /**
     * Converts a {@link Exercise} domain aggregate to a {@link ExerciseEntity}.
     *
     * @param domain the aggregate; must not be {@code null}
     * @return a JPA entity ready for persistence
     */
    public ExerciseEntity toEntity(Exercise domain) {
        Objects.requireNonNull(domain, "Exercise domain must not be null");

        return ExerciseEntity.builder()
                .id(domain.getId().value())
                .name(domain.getName())
                .description(domain.getDescription())
                .level(domain.getLevel())
                .primaryMuscle(domain.getPrimaryMuscle())
                .routines(domain.getRoutines().isEmpty()
                        ? Set.of()
                        : domain.getRoutines().stream().collect(Collectors.toUnmodifiableSet()))
                .secondaryMuscles(domain.getSecondaryMuscles().isEmpty()
                        ? Set.of()
                        : domain.getSecondaryMuscles().stream().collect(Collectors.toUnmodifiableSet()))
                .image(toMediaEntity(domain.getImage()))
                .video(toMediaEntity(domain.getVideo()))
                .audit(AuditEmbeddable.fromDomain(domain.getAuditFields()))
                .build();
    }

    // -------------------------------------------------------------------------
    // Entity → Domain
    // -------------------------------------------------------------------------

    /**
     * Reconstitutes an {@link Exercise} aggregate from a {@link ExerciseEntity}.
     *
     * <p>Uses the package-private reconstitution constructor — bypasses business
     * invariant checks since the data is already trusted from persistence.
     *
     * @param entity the JPA entity; must not be {@code null}
     * @return a fully reconstituted domain aggregate
     */
    public Exercise toDomain(ExerciseEntity entity) {
        Objects.requireNonNull(entity, "ExerciseEntity must not be null");

        List<ExerciseSeriesId> exerciseSeriesIds = entity.getExerciseSeriesEntities()
                .stream()
                .map(es -> ExerciseSeriesId.of(es.getId()))
                .toList();

        AuditFields audit = entity.getAudit() != null
                ? entity.getAudit().toDomain()
                : null;

        return Exercise.reconstitute(
                ExerciseId.of(entity.getId()),
                entity.getName(),
                entity.getDescription(),
                entity.getLevel(),
                entity.getRoutines(),
                entity.getPrimaryMuscle(),
                entity.getSecondaryMuscles(),
                toMedia(entity.getImage()),
                toMedia(entity.getVideo()),
                exerciseSeriesIds,
                audit
        );
    }

    // -------------------------------------------------------------------------
    // Entity → Detail View (CQRS query side)
    // -------------------------------------------------------------------------

    /**
     * Converts a {@link ExerciseEntity} to a {@link ExerciseDetailView} read model.
     *
     * @param entity the JPA entity; must not be {@code null}
     * @return a fully populated detail view
     */
    public ExerciseDetailView toDetailView(ExerciseEntity entity) {
        Objects.requireNonNull(entity, "ExerciseEntity must not be null");

        return new ExerciseDetailView(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getLevel(),
                entity.getRoutines(),
                entity.getPrimaryMuscle(),
                entity.getSecondaryMuscles(),
                toMediaView(entity.getImage()),
                toMediaView(entity.getVideo())
        );
    }

    // -------------------------------------------------------------------------
    // Entity → Summary View (CQRS query side)
    // -------------------------------------------------------------------------

    /**
     * Converts a {@link ExerciseEntity} to a lightweight {@link ExerciseSummaryView}.
     *
     * @param entity the JPA entity; must not be {@code null}
     * @return a lightweight summary view
     */
    public ExerciseSummaryView toSummaryView(ExerciseEntity entity) {
        Objects.requireNonNull(entity, "ExerciseEntity must not be null");

        return new ExerciseSummaryView(
                entity.getId(),
                entity.getName(),
                entity.getLevel(),
                entity.getPrimaryMuscle(),
                entity.getRoutines()
        );
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private static MediaEntity toMediaEntity(Media media) {
        if (media == null) return null;
        return MediaEntity.builder()
                .id(UUID.randomUUID())   // ID assigned here only if new; adapter handles dedup
                .name(media.name())
                .description(media.description())
                .url(media.url())
                .build();
    }

    private static Media toMedia(MediaEntity entity) {
        if (entity == null) return null;
        return Media.of(entity.getName(), entity.getDescription(), entity.getUrl());
    }

    private static MediaView toMediaView(MediaEntity entity) {
        if (entity == null) return null;
        return new MediaView(entity.getName(), entity.getDescription(), entity.getUrl());
    }
}



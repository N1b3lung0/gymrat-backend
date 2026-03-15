package com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence.mapper;

import com.n1b3lung0.gymrat.application.dto.ExerciseSeriesDetailView;
import com.n1b3lung0.gymrat.application.dto.ExerciseSeriesSummaryView;
import com.n1b3lung0.gymrat.domain.model.ExerciseId;
import com.n1b3lung0.gymrat.domain.model.ExerciseSeries;
import com.n1b3lung0.gymrat.domain.model.ExerciseSeriesId;
import com.n1b3lung0.gymrat.domain.model.SeriesId;
import com.n1b3lung0.gymrat.domain.model.WorkoutId;
import com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence.entity.AuditEmbeddable;
import com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence.entity.ExerciseSeriesEntity;
import com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence.entity.SeriesEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Hand-written mapper between the {@link ExerciseSeries} domain aggregate and
 * the {@link ExerciseSeriesEntity} JPA entity / application read-model views.
 */
@Component
public class ExerciseSeriesPersistenceMapper {

    // -------------------------------------------------------------------------
    // Domain → Entity
    // -------------------------------------------------------------------------

    /**
     * Converts an {@link ExerciseSeries} domain aggregate to an {@link ExerciseSeriesEntity}.
     *
     * <p>The {@code workout} and {@code exercise} FK references are set by the JPA adapter
     * (not here) since we only carry IDs on the domain side.
     *
     * @param domain the aggregate; must not be {@code null}
     * @return a JPA entity with {@code workout} and {@code exercise} left {@code null}
     *         (the adapter must set them before saving)
     */
    public ExerciseSeriesEntity toEntity(ExerciseSeries domain) {
        Objects.requireNonNull(domain, "ExerciseSeries domain must not be null");

        return ExerciseSeriesEntity.builder()
                .id(domain.getId().value())
                // workout and exercise FK entities set by the adapter
                .audit(AuditEmbeddable.fromDomain(domain.getAuditFields()))
                .build();
    }

    // -------------------------------------------------------------------------
    // Entity → Domain
    // -------------------------------------------------------------------------

    /**
     * Reconstitutes an {@link ExerciseSeries} aggregate from an {@link ExerciseSeriesEntity}.
     *
     * @param entity the JPA entity; must not be {@code null}
     * @return a fully reconstituted domain aggregate
     */
    public ExerciseSeries toDomain(ExerciseSeriesEntity entity) {
        Objects.requireNonNull(entity, "ExerciseSeriesEntity must not be null");

        List<SeriesId> seriesIds = entity.getSeriesEntities()
                .stream()
                .map(s -> SeriesId.of(s.getId()))
                .toList();

        return ExerciseSeries.reconstitute(
                ExerciseSeriesId.of(entity.getId()),
                WorkoutId.of(entity.getWorkout().getId()),
                ExerciseId.of(entity.getExercise().getId()),
                seriesIds,
                entity.getAudit() != null ? entity.getAudit().toDomain() : null
        );
    }

    // -------------------------------------------------------------------------
    // Entity → Detail View (CQRS query side)
    // -------------------------------------------------------------------------

    /**
     * Converts an {@link ExerciseSeriesEntity} to an {@link ExerciseSeriesDetailView}.
     *
     * @param entity the JPA entity; must not be {@code null}
     * @return a fully populated detail view
     */
    public ExerciseSeriesDetailView toDetailView(ExerciseSeriesEntity entity) {
        Objects.requireNonNull(entity, "ExerciseSeriesEntity must not be null");

        List<UUID> seriesIds = entity.getSeriesEntities()
                .stream()
                .map(SeriesEntity::getId)
                .toList();

        return new ExerciseSeriesDetailView(
                entity.getId(),
                entity.getWorkout().getId(),
                entity.getExercise().getId(),
                seriesIds
        );
    }

    // -------------------------------------------------------------------------
    // Entity → Summary View (CQRS query side)
    // -------------------------------------------------------------------------

    /**
     * Converts an {@link ExerciseSeriesEntity} to a lightweight {@link ExerciseSeriesSummaryView}.
     *
     * @param entity the JPA entity; must not be {@code null}
     * @return a lightweight summary view
     */
    public ExerciseSeriesSummaryView toSummaryView(ExerciseSeriesEntity entity) {
        Objects.requireNonNull(entity, "ExerciseSeriesEntity must not be null");

        return new ExerciseSeriesSummaryView(
                entity.getId(),
                entity.getExercise().getId(),
                entity.getSeriesEntities().size()
        );
    }
}


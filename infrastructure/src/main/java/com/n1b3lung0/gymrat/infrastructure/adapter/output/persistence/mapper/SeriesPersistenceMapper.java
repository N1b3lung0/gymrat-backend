package com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence.mapper;

import com.n1b3lung0.gymrat.application.dto.SeriesDetailView;
import com.n1b3lung0.gymrat.application.dto.SeriesSummaryView;
import com.n1b3lung0.gymrat.domain.model.ExerciseSeriesId;
import com.n1b3lung0.gymrat.domain.model.RestTime;
import com.n1b3lung0.gymrat.domain.model.Series;
import com.n1b3lung0.gymrat.domain.model.SeriesId;
import com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence.entity.AuditEmbeddable;
import com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence.entity.SeriesEntity;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Hand-written mapper between the {@link Series} domain aggregate and
 * the {@link SeriesEntity} JPA entity / application read-model views.
 *
 * <p>{@code restTime} is stored in the database as an integer (seconds).
 * Conversion uses {@link RestTime#fromSeconds(int)} and {@link RestTime#getSeconds()}.
 */
@Component
public class SeriesPersistenceMapper {

    // -------------------------------------------------------------------------
    // Domain → Entity
    // -------------------------------------------------------------------------

    /**
     * Converts a {@link Series} domain aggregate to a {@link SeriesEntity}.
     *
     * <p>The {@code exerciseSeries} FK reference is set by the JPA adapter
     * (not here) since we only carry IDs on the domain side.
     *
     * @param domain the aggregate; must not be {@code null}
     * @return a JPA entity with {@code exerciseSeries} left {@code null}
     *         (the adapter must set it before saving)
     */
    public SeriesEntity toEntity(Series domain) {
        Objects.requireNonNull(domain, "Series domain must not be null");

        return SeriesEntity.builder()
                .id(domain.getId().value())
                .serialNumber(domain.getSerialNumber())
                .repetitionsToDo(domain.getRepetitionsToDo())
                .repetitionsDone(domain.getRepetitionsDone())
                .intensity(domain.getIntensity())
                .weight(domain.getWeight())
                .startSeries(domain.getStartSeries())
                .endSeries(domain.getEndSeries())
                .restTime(domain.getRestTime().getSeconds())
                // exerciseSeries FK entity set by the adapter
                .audit(AuditEmbeddable.fromDomain(domain.getAuditFields()))
                .build();
    }

    // -------------------------------------------------------------------------
    // Entity → Domain
    // -------------------------------------------------------------------------

    /**
     * Reconstitutes a {@link Series} aggregate from a {@link SeriesEntity}.
     *
     * @param entity the JPA entity; must not be {@code null}
     * @return a fully reconstituted domain aggregate
     */
    public Series toDomain(SeriesEntity entity) {
        Objects.requireNonNull(entity, "SeriesEntity must not be null");

        return Series.reconstitute(
                SeriesId.of(entity.getId()),
                entity.getSerialNumber(),
                entity.getRepetitionsToDo(),
                entity.getRepetitionsDone(),
                entity.getIntensity(),
                entity.getWeight(),
                entity.getStartSeries(),
                entity.getEndSeries(),
                RestTime.fromSeconds(entity.getRestTime()),
                ExerciseSeriesId.of(entity.getExerciseSeries().getId()),
                entity.getAudit() != null ? entity.getAudit().toDomain() : null
        );
    }

    // -------------------------------------------------------------------------
    // Entity → Detail View (CQRS query side)
    // -------------------------------------------------------------------------

    /**
     * Converts a {@link SeriesEntity} to a {@link SeriesDetailView} read model.
     *
     * @param entity the JPA entity; must not be {@code null}
     * @return a fully populated detail view
     */
    public SeriesDetailView toDetailView(SeriesEntity entity) {
        Objects.requireNonNull(entity, "SeriesEntity must not be null");

        return new SeriesDetailView(
                entity.getId(),
                entity.getSerialNumber(),
                entity.getRepetitionsToDo(),
                entity.getRepetitionsDone(),
                entity.getIntensity(),
                entity.getWeight(),
                entity.getStartSeries(),
                entity.getEndSeries(),
                RestTime.fromSeconds(entity.getRestTime()),
                entity.getExerciseSeries().getId()
        );
    }

    // -------------------------------------------------------------------------
    // Entity → Summary View (CQRS query side)
    // -------------------------------------------------------------------------

    /**
     * Converts a {@link SeriesEntity} to a lightweight {@link SeriesSummaryView}.
     *
     * @param entity the JPA entity; must not be {@code null}
     * @return a lightweight summary view
     */
    public SeriesSummaryView toSummaryView(SeriesEntity entity) {
        Objects.requireNonNull(entity, "SeriesEntity must not be null");

        return new SeriesSummaryView(
                entity.getId(),
                entity.getSerialNumber(),
                entity.getRepetitionsToDo(),
                entity.getIntensity(),
                entity.getWeight(),
                RestTime.fromSeconds(entity.getRestTime())
        );
    }
}


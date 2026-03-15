package com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence.mapper;

import com.n1b3lung0.gymrat.application.dto.WorkoutDetailView;
import com.n1b3lung0.gymrat.application.dto.WorkoutSummaryView;
import com.n1b3lung0.gymrat.domain.model.ExerciseSeriesId;
import com.n1b3lung0.gymrat.domain.model.Workout;
import com.n1b3lung0.gymrat.domain.model.WorkoutId;
import com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence.entity.AuditEmbeddable;
import com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence.entity.ExerciseSeriesEntity;
import com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence.entity.WorkoutEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Hand-written mapper between the {@link Workout} domain aggregate and
 * the {@link WorkoutEntity} JPA entity / application read-model views.
 */
@Component
public class WorkoutPersistenceMapper {

    // -------------------------------------------------------------------------
    // Domain → Entity
    // -------------------------------------------------------------------------

    /**
     * Converts a {@link Workout} domain aggregate to a {@link WorkoutEntity}.
     *
     * @param domain the aggregate; must not be {@code null}
     * @return a JPA entity ready for persistence
     */
    public WorkoutEntity toEntity(Workout domain) {
        Objects.requireNonNull(domain, "Workout domain must not be null");

        return WorkoutEntity.builder()
                .id(domain.getId().value())
                .startWorkout(domain.getStartWorkout())
                .endWorkout(domain.getEndWorkout())
                .audit(AuditEmbeddable.fromDomain(domain.getAuditFields()))
                .build();
    }

    // -------------------------------------------------------------------------
    // Entity → Domain
    // -------------------------------------------------------------------------

    /**
     * Reconstitutes a {@link Workout} aggregate from a {@link WorkoutEntity}.
     *
     * @param entity the JPA entity; must not be {@code null}
     * @return a fully reconstituted domain aggregate
     */
    public Workout toDomain(WorkoutEntity entity) {
        Objects.requireNonNull(entity, "WorkoutEntity must not be null");

        List<ExerciseSeriesId> exerciseSeriesIds = entity.getExerciseSeriesEntities()
                .stream()
                .map(es -> ExerciseSeriesId.of(es.getId()))
                .toList();

        return Workout.reconstitute(
                WorkoutId.of(entity.getId()),
                entity.getStartWorkout(),
                entity.getEndWorkout(),
                exerciseSeriesIds,
                entity.getAudit() != null ? entity.getAudit().toDomain() : null
        );
    }

    // -------------------------------------------------------------------------
    // Entity → Detail View (CQRS query side)
    // -------------------------------------------------------------------------

    /**
     * Converts a {@link WorkoutEntity} to a {@link WorkoutDetailView} read model.
     *
     * @param entity the JPA entity; must not be {@code null}
     * @return a fully populated detail view
     */
    public WorkoutDetailView toDetailView(WorkoutEntity entity) {
        Objects.requireNonNull(entity, "WorkoutEntity must not be null");

        List<java.util.UUID> exerciseSeriesIds = entity.getExerciseSeriesEntities()
                .stream()
                .map(ExerciseSeriesEntity::getId)
                .toList();

        return new WorkoutDetailView(
                entity.getId(),
                entity.getStartWorkout(),
                entity.getEndWorkout(),
                exerciseSeriesIds
        );
    }

    // -------------------------------------------------------------------------
    // Entity → Summary View (CQRS query side)
    // -------------------------------------------------------------------------

    /**
     * Converts a {@link WorkoutEntity} to a lightweight {@link WorkoutSummaryView}.
     *
     * @param entity the JPA entity; must not be {@code null}
     * @return a lightweight summary view
     */
    public WorkoutSummaryView toSummaryView(WorkoutEntity entity) {
        Objects.requireNonNull(entity, "WorkoutEntity must not be null");

        return new WorkoutSummaryView(
                entity.getId(),
                entity.getStartWorkout(),
                entity.getEndWorkout(),
                entity.getEndWorkout() != null
        );
    }
}


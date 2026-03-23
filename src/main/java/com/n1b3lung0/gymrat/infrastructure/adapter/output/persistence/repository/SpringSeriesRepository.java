package com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence.repository;

import com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence.entity.SeriesEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link SeriesEntity}.
 */
public interface SpringSeriesRepository extends JpaRepository<SeriesEntity, UUID> {

    /**
     * Returns all active series belonging to the given exercise-series,
     * ordered by serial_number ascending.
     * The {@code @SQLRestriction} on the entity filters deleted rows automatically.
     */
    List<SeriesEntity> findAllByExerciseSeries_IdOrderBySerialNumberAsc(UUID exerciseSeriesId);

    /**
     * Returns the count of active series within the given exercise-series.
     * Used by the application layer to auto-compute the next {@code serialNumber}.
     */
    long countByExerciseSeries_Id(UUID exerciseSeriesId);
}


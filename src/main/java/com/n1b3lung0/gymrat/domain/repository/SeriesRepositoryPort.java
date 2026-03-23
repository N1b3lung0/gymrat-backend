package com.n1b3lung0.gymrat.domain.repository;

import com.n1b3lung0.gymrat.domain.model.ExerciseSeriesId;
import com.n1b3lung0.gymrat.domain.model.Series;
import com.n1b3lung0.gymrat.domain.model.SeriesId;

import java.util.List;
import java.util.Optional;

/**
 * Output port — persistence contract for the {@link Series} aggregate.
 *
 * <p>Implemented in the infrastructure layer by a JPA adapter.
 */
public interface SeriesRepositoryPort {

    /**
     * Persists a new or updated {@link Series}.
     *
     * @param series the aggregate to save
     * @return the saved aggregate
     */
    Series save(Series series);

    /**
     * Returns the {@link Series} with the given identifier, if it exists.
     *
     * @param id the series identifier
     * @return an {@link Optional} containing the aggregate, or empty if not found
     */
    Optional<Series> findById(SeriesId id);

    /**
     * Returns all {@link Series} belonging to the given {@link ExerciseSeriesId},
     * ordered by {@code serialNumber} ascending.
     *
     * @param exerciseSeriesId the parent exercise-series identifier
     * @return ordered list of series; never {@code null}
     */
    List<Series> findAllByExerciseSeriesId(ExerciseSeriesId exerciseSeriesId);

    /**
     * Removes the {@link Series} with the given identifier (soft-delete).
     *
     * @param id the series identifier
     */
    void deleteById(SeriesId id);

    /**
     * Returns the number of active {@link Series} within the given
     * {@link ExerciseSeriesId}.
     *
     * <p>Used by the application layer to compute the next {@code serialNumber}
     * when creating a new series: {@code nextSerialNumber = count + 1}.
     *
     * @param exerciseSeriesId the parent exercise-series identifier
     * @return count of active series
     */
    long countByExerciseSeriesId(ExerciseSeriesId exerciseSeriesId);
}


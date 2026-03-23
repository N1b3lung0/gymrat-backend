package com.n1b3lung0.gymrat.domain.repository;

import com.n1b3lung0.gymrat.domain.model.ExerciseSeries;
import com.n1b3lung0.gymrat.domain.model.ExerciseSeriesId;
import com.n1b3lung0.gymrat.domain.model.WorkoutId;

import java.util.List;
import java.util.Optional;

/**
 * Output port — persistence contract for the {@link ExerciseSeries} aggregate.
 *
 * <p>Implemented in the infrastructure layer by a JPA adapter.
 */
public interface ExerciseSeriesRepositoryPort {

    /**
     * Persists a new or updated {@link ExerciseSeries}.
     *
     * @param exerciseSeries the aggregate to save
     * @return the saved aggregate
     */
    ExerciseSeries save(ExerciseSeries exerciseSeries);

    /**
     * Returns the {@link ExerciseSeries} with the given identifier, if it exists.
     *
     * @param id the exercise-series identifier
     * @return an {@link Optional} containing the aggregate, or empty if not found
     */
    Optional<ExerciseSeries> findById(ExerciseSeriesId id);

    /**
     * Returns all {@link ExerciseSeries} belonging to the given {@link WorkoutId}.
     *
     * @param workoutId the parent workout identifier
     * @return list of exercise-series; never {@code null}
     */
    List<ExerciseSeries> findAllByWorkoutId(WorkoutId workoutId);

    /**
     * Removes the {@link ExerciseSeries} with the given identifier (soft-delete).
     *
     * @param id the exercise-series identifier
     */
    void deleteById(ExerciseSeriesId id);
}


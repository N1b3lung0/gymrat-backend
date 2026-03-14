package com.n1b3lung0.gymrat.domain.repository;

import com.n1b3lung0.gymrat.domain.model.Workout;
import com.n1b3lung0.gymrat.domain.model.WorkoutId;

import java.util.Optional;

/**
 * Output port — persistence contract for the {@link Workout} aggregate.
 *
 * <p>Implemented in the infrastructure layer by a JPA adapter.
 */
public interface WorkoutRepositoryPort {

    /**
     * Persists a new or updated {@link Workout}.
     *
     * @param workout the aggregate to save
     * @return the saved aggregate
     */
    Workout save(Workout workout);

    /**
     * Returns the {@link Workout} with the given identifier, if it exists.
     *
     * @param id the workout identifier
     * @return an {@link Optional} containing the aggregate, or empty if not found
     */
    Optional<Workout> findById(WorkoutId id);

    /**
     * Returns a paginated slice of all active workouts.
     *
     * @param pageRequest pagination and sorting parameters
     * @return a {@link PageResult} of workouts
     */
    PageResult<Workout> findAll(PageRequest pageRequest);

    /**
     * Removes the {@link Workout} with the given identifier (soft-delete).
     *
     * @param id the workout identifier
     */
    void deleteById(WorkoutId id);
}


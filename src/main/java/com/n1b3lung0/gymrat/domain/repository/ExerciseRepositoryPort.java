package com.n1b3lung0.gymrat.domain.repository;

import com.n1b3lung0.gymrat.domain.model.Exercise;
import com.n1b3lung0.gymrat.domain.model.ExerciseId;

import java.util.Optional;

/**
 * Output port — persistence contract for the {@link Exercise} aggregate.
 *
 * <p>Implemented in the infrastructure layer by a JPA adapter.
 * The domain must never depend on the adapter — only on this interface.
 */
public interface ExerciseRepositoryPort {

    /**
     * Persists a new or updated {@link Exercise}.
     *
     * @param exercise the aggregate to save
     * @return the saved aggregate (may carry generated/updated fields)
     */
    Exercise save(Exercise exercise);

    /**
     * Returns the {@link Exercise} with the given identifier, if it exists.
     *
     * @param id the exercise identifier
     * @return an {@link Optional} containing the aggregate, or empty if not found
     */
    Optional<Exercise> findById(ExerciseId id);

    /**
     * Returns a paginated slice of all active exercises.
     *
     * @param pageRequest pagination and sorting parameters
     * @return a {@link PageResult} of exercises
     */
    PageResult<Exercise> findAll(PageRequest pageRequest);

    /**
     * Removes the {@link Exercise} with the given identifier.
     *
     * <p>Implementations must apply soft-delete semantics: the record is marked
     * inactive rather than physically removed.
     *
     * @param id the exercise identifier
     */
    void deleteById(ExerciseId id);

    /**
     * Returns {@code true} if an active {@link Exercise} with the given name already exists.
     *
     * @param name the exercise name to check
     * @return {@code true} if a duplicate name exists
     */
    boolean existsByName(String name);
}




package com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence.repository;

import com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence.entity.ExerciseSeriesEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link ExerciseSeriesEntity}.
 */
public interface SpringExerciseSeriesRepository extends JpaRepository<ExerciseSeriesEntity, UUID> {

    /**
     * Loads the exercise-series with workout and exercise associations eagerly
     * to avoid N+1 problems when reconstituting the domain aggregate.
     */
    @EntityGraph(attributePaths = {"workout", "exercise"})
    Optional<ExerciseSeriesEntity> findWithAllById(UUID id);

    /**
     * Returns all active exercise-series belonging to the given workout.
     * The {@code @SQLRestriction} on the entity filters deleted rows automatically.
     */
    List<ExerciseSeriesEntity> findAllByWorkout_Id(UUID workoutId);
}


package com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence.repository;

import com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence.entity.ExerciseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link ExerciseEntity}.
 */
public interface SpringExerciseRepository extends JpaRepository<ExerciseEntity, UUID> {

    /**
     * Loads the exercise with all associations eagerly (routines, secondaryMuscles,
     * image, video) in a single query to avoid N+1 problems.
     */
    @EntityGraph(attributePaths = {"routines", "secondaryMuscles", "image", "video"})
    Optional<ExerciseEntity> findWithAllById(UUID id);

    /**
     * Returns {@code true} if an active exercise with the given name already exists.
     * The {@code @SQLRestriction} on the entity filters deleted rows automatically.
     */
    boolean existsByName(String name);

    /**
     * Returns a paginated slice of all active exercises.
     * The {@code @SQLRestriction} on the entity filters deleted rows automatically.
     */
    Page<ExerciseEntity> findAll(Pageable pageable);
}


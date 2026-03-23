package com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence.repository;

import com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence.entity.WorkoutEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Spring Data JPA repository for {@link WorkoutEntity}.
 */
public interface SpringWorkoutRepository extends JpaRepository<WorkoutEntity, UUID> {

    /**
     * Returns a paginated slice of all active workouts ordered by start_workout DESC.
     * The {@code @SQLRestriction} on the entity filters deleted rows automatically.
     */
    Page<WorkoutEntity> findAll(Pageable pageable);
}


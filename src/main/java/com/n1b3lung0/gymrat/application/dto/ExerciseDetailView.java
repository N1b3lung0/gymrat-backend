package com.n1b3lung0.gymrat.application.dto;

import com.n1b3lung0.gymrat.domain.model.Level;
import com.n1b3lung0.gymrat.domain.model.Muscle;
import com.n1b3lung0.gymrat.domain.model.Routine;

import java.util.Set;
import java.util.UUID;

/**
 * Read model returned by {@code GetExerciseByIdUseCase}.
 *
 * <p>Carries all exercise fields including media assets and muscle groups.
 * Built directly from persistence projections — never from domain aggregates
 * on the query side (CQRS).
 *
 * @param id               exercise UUID
 * @param name             exercise name
 * @param description      optional description
 * @param level            difficulty level
 * @param routines         routine types this exercise belongs to
 * @param primaryMuscle    main muscle targeted
 * @param secondaryMuscles secondary muscles targeted
 * @param image            optional image asset
 * @param video            optional video asset
 */
public record ExerciseDetailView(
        UUID id,
        String name,
        String description,
        Level level,
        Set<Routine> routines,
        Muscle primaryMuscle,
        Set<Muscle> secondaryMuscles,
        MediaView image,
        MediaView video
) {}


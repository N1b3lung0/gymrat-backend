package com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto;

import com.n1b3lung0.gymrat.domain.model.Level;
import com.n1b3lung0.gymrat.domain.model.Muscle;
import com.n1b3lung0.gymrat.domain.model.Routine;

import java.util.Set;
import java.util.UUID;

/**
 * REST response DTO carrying the full detail of an exercise.
 *
 * <p>Returned by {@code GET /api/v1/exercises/{id}} and {@code PUT /api/v1/exercises/{id}}.
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
public record ExerciseResponse(
        UUID id,
        String name,
        String description,
        Level level,
        Set<Routine> routines,
        Muscle primaryMuscle,
        Set<Muscle> secondaryMuscles,
        MediaResponse image,
        MediaResponse video
) {}


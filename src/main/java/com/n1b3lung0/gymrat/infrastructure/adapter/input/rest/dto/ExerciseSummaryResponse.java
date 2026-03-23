package com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto;

import com.n1b3lung0.gymrat.domain.model.Level;
import com.n1b3lung0.gymrat.domain.model.Muscle;
import com.n1b3lung0.gymrat.domain.model.Routine;

import java.util.Set;
import java.util.UUID;

/**
 * Lightweight REST response DTO for paginated exercise listings.
 *
 * <p>Intentionally omits media assets and secondary muscles to keep list
 * responses lean. Use {@link ExerciseResponse} for full detail.
 *
 * @param id            exercise UUID
 * @param name          exercise name
 * @param level         difficulty level
 * @param primaryMuscle main muscle targeted
 * @param routines      routine types this exercise belongs to
 */
public record ExerciseSummaryResponse(
        UUID id,
        String name,
        Level level,
        Muscle primaryMuscle,
        Set<Routine> routines
) {}


package com.n1b3lung0.gymrat.application.dto;

import com.n1b3lung0.gymrat.domain.model.Level;
import com.n1b3lung0.gymrat.domain.model.Muscle;
import com.n1b3lung0.gymrat.domain.model.Routine;

import java.util.Set;
import java.util.UUID;

/**
 * Command to create a new exercise.
 *
 * @param name             exercise name; must not be blank
 * @param description      optional description
 * @param level            difficulty level
 * @param routines         routine types; must not be empty
 * @param primaryMuscle    primary muscle targeted
 * @param secondaryMuscles secondary muscles targeted; may be empty
 * @param imageName        optional image asset name
 * @param imageDescription optional image asset description
 * @param imageUrl         optional image asset URL
 * @param videoName        optional video asset name
 * @param videoDescription optional video asset description
 * @param videoUrl         optional video asset URL
 * @param idempotencyKey   client-supplied key for idempotent creation; may be {@code null}
 */
public record CreateExerciseCommand(
        String name,
        String description,
        Level level,
        Set<Routine> routines,
        Muscle primaryMuscle,
        Set<Muscle> secondaryMuscles,
        String imageName,
        String imageDescription,
        String imageUrl,
        String videoName,
        String videoDescription,
        String videoUrl,
        UUID idempotencyKey
) {}


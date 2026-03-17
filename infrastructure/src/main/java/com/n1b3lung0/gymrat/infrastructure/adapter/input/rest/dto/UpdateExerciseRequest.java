package com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto;

import com.n1b3lung0.gymrat.domain.model.Level;
import com.n1b3lung0.gymrat.domain.model.Muscle;
import com.n1b3lung0.gymrat.domain.model.Routine;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

/**
 * REST request DTO for updating an existing exercise.
 *
 * <p>All fields are required — this is a full replacement ({@code PUT} semantics).
 *
 * @param name             new exercise name; must not be blank
 * @param description      new description; may be null to clear it
 * @param level            new difficulty level; must not be null
 * @param routines         new routine types; must not be empty
 * @param primaryMuscle    new primary muscle; must not be null
 * @param secondaryMuscles new secondary muscles; may be empty
 * @param image            new image asset; may be null to clear it
 * @param video            new video asset; may be null to clear it
 */
public record UpdateExerciseRequest(
        @NotBlank(message = "Exercise name must not be blank")
        @Size(max = 255, message = "Exercise name must not exceed 255 characters")
        String name,

        @Size(max = 5000, message = "Description must not exceed 5000 characters")
        String description,

        @NotNull(message = "Exercise level must not be null")
        Level level,

        @NotEmpty(message = "Exercise must belong to at least one routine")
        Set<@NotNull Routine> routines,

        @NotNull(message = "Primary muscle must not be null")
        Muscle primaryMuscle,

        Set<@NotNull Muscle> secondaryMuscles,

        @Valid
        MediaRequest image,

        @Valid
        MediaRequest video
) {}


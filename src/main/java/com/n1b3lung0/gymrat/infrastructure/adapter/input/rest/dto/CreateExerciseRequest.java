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
 * REST request DTO for creating a new exercise.
 *
 * <p>Bean Validation annotations enforce input constraints before the command
 * reaches the application layer.
 *
 * @param name             exercise name; must not be blank
 * @param description      optional description
 * @param level            difficulty level; must not be null
 * @param routines         routine types; must not be empty
 * @param primaryMuscle    primary muscle targeted; must not be null
 * @param secondaryMuscles secondary muscles targeted; may be empty
 * @param image            optional image asset
 * @param video            optional video asset
 */
public record CreateExerciseRequest(
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


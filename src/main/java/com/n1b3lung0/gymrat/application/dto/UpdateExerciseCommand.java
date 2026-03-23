package com.n1b3lung0.gymrat.application.dto;

import com.n1b3lung0.gymrat.domain.model.ExerciseId;
import com.n1b3lung0.gymrat.domain.model.Level;
import com.n1b3lung0.gymrat.domain.model.Muscle;
import com.n1b3lung0.gymrat.domain.model.Routine;

import java.util.Set;

/**
 * Command to update an existing exercise.
 *
 * @param id               identifier of the exercise to update
 * @param name             new name; must not be blank
 * @param description      new description
 * @param level            new difficulty level
 * @param routines         new routine types; must not be empty
 * @param primaryMuscle    new primary muscle
 * @param secondaryMuscles new secondary muscles; may be empty
 * @param imageName        new image asset name
 * @param imageDescription new image asset description
 * @param imageUrl         new image asset URL
 * @param videoName        new video asset name
 * @param videoDescription new video asset description
 * @param videoUrl         new video asset URL
 */
public record UpdateExerciseCommand(
        ExerciseId id,
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
        String videoUrl
) {}


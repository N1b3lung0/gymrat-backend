package com.n1b3lung0.gymrat.application.port.input.command;

import com.n1b3lung0.gymrat.application.dto.AddExerciseToWorkoutCommand;
import com.n1b3lung0.gymrat.domain.model.ExerciseSeriesId;

/** Input port — use case for adding an exercise to a workout. */
public interface AddExerciseToWorkoutUseCase {
    ExerciseSeriesId execute(AddExerciseToWorkoutCommand command);
}


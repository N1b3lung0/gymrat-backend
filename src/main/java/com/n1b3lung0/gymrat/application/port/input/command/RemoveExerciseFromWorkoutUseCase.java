package com.n1b3lung0.gymrat.application.port.input.command;

import com.n1b3lung0.gymrat.application.dto.RemoveExerciseFromWorkoutCommand;

/** Input port — use case for removing an exercise session from a workout. */
public interface RemoveExerciseFromWorkoutUseCase {
    void execute(RemoveExerciseFromWorkoutCommand command);
}


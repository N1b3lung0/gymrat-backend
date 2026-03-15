package com.n1b3lung0.gymrat.application.port.input.command;

import com.n1b3lung0.gymrat.application.dto.UpdateExerciseCommand;

/**
 * Input port — use case for updating an existing exercise.
 */
public interface UpdateExerciseUseCase {

    /**
     * Executes the update exercise use case.
     *
     * @param command the update command carrying the exercise id and new field values
     */
    void execute(UpdateExerciseCommand command);
}


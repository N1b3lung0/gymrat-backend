package com.n1b3lung0.gymrat.application.port.input.command;

import com.n1b3lung0.gymrat.application.dto.DeleteExerciseCommand;

/**
 * Input port — use case for soft-deleting an exercise.
 */
public interface DeleteExerciseUseCase {

    /**
     * Executes the delete exercise use case.
     *
     * @param command the delete command carrying the exercise id
     */
    void execute(DeleteExerciseCommand command);
}


package com.n1b3lung0.gymrat.application.port.input.command;

import com.n1b3lung0.gymrat.application.dto.CreateExerciseCommand;
import com.n1b3lung0.gymrat.domain.model.ExerciseId;

/**
 * Input port — use case for creating a new exercise.
 */
public interface CreateExerciseUseCase {

    /**
     * Executes the create exercise use case.
     *
     * @param command the creation command carrying all required fields
     * @return the generated {@link ExerciseId} of the newly created exercise
     */
    ExerciseId execute(CreateExerciseCommand command);
}


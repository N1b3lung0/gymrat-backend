package com.n1b3lung0.gymrat.application.command;

import com.n1b3lung0.gymrat.application.dto.RemoveExerciseFromWorkoutCommand;
import com.n1b3lung0.gymrat.application.port.input.command.RemoveExerciseFromWorkoutUseCase;
import com.n1b3lung0.gymrat.domain.exception.ExerciseSeriesNotFoundException;
import com.n1b3lung0.gymrat.domain.repository.ExerciseSeriesRepositoryPort;

import java.util.Objects;

/**
 * Handles the {@link RemoveExerciseFromWorkoutCommand} use case.
 *
 * <ol>
 *   <li>Verifies the exercise-series exists.
 *   <li>Delegates soft-delete to the repository port.
 * </ol>
 */
public class RemoveExerciseFromWorkoutHandler implements RemoveExerciseFromWorkoutUseCase {

    private final ExerciseSeriesRepositoryPort exerciseSeriesRepository;

    public RemoveExerciseFromWorkoutHandler(ExerciseSeriesRepositoryPort exerciseSeriesRepository) {
        this.exerciseSeriesRepository = Objects.requireNonNull(exerciseSeriesRepository);
    }

    @Override
    public void execute(RemoveExerciseFromWorkoutCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        if (exerciseSeriesRepository.findById(command.id()).isEmpty()) {
            throw new ExerciseSeriesNotFoundException(command.id());
        }

        exerciseSeriesRepository.deleteById(command.id());
    }
}


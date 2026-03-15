package com.n1b3lung0.gymrat.application.command;

import com.n1b3lung0.gymrat.application.dto.DeleteExerciseCommand;
import com.n1b3lung0.gymrat.application.port.input.command.DeleteExerciseUseCase;
import com.n1b3lung0.gymrat.application.port.output.DomainEventPublisherPort;
import com.n1b3lung0.gymrat.domain.exception.ExerciseNotFoundException;
import com.n1b3lung0.gymrat.domain.repository.ExerciseRepositoryPort;

import java.util.Objects;

/**
 * Handles the {@link DeleteExerciseCommand} use case.
 *
 * <ol>
 *   <li>Loads the existing {@link com.n1b3lung0.gymrat.domain.model.Exercise} or throws
 *       {@link ExerciseNotFoundException}.
 *   <li>Calls {@code exercise.delete()} to apply soft-delete and accumulate the
 *       {@link com.n1b3lung0.gymrat.domain.event.ExerciseDeleted} event.
 *   <li>Persists the aggregate (soft-delete state).
 *   <li>Publishes accumulated domain events.
 * </ol>
 */
public class DeleteExerciseHandler implements DeleteExerciseUseCase {

    private final ExerciseRepositoryPort exerciseRepository;
    private final DomainEventPublisherPort eventPublisher;

    public DeleteExerciseHandler(
            ExerciseRepositoryPort exerciseRepository,
            DomainEventPublisherPort eventPublisher) {
        this.exerciseRepository = Objects.requireNonNull(exerciseRepository);
        this.eventPublisher     = Objects.requireNonNull(eventPublisher);
    }

    @Override
    public void execute(DeleteExerciseCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        // 1. Load aggregate
        var exercise = exerciseRepository.findById(command.id())
                .orElseThrow(() -> new ExerciseNotFoundException(command.id()));

        // 2. Apply soft-delete (also accumulates ExerciseDeleted event)
        exercise.delete();

        // 3. Persist soft-deleted state
        exerciseRepository.save(exercise);

        // 4. Publish domain events (always after save)
        exercise.pullDomainEvents().forEach(eventPublisher::publish);
    }
}


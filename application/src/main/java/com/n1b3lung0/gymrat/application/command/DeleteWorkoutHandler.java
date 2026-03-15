package com.n1b3lung0.gymrat.application.command;

import com.n1b3lung0.gymrat.application.dto.DeleteWorkoutCommand;
import com.n1b3lung0.gymrat.application.port.input.command.DeleteWorkoutUseCase;
import com.n1b3lung0.gymrat.application.port.output.DomainEventPublisherPort;
import com.n1b3lung0.gymrat.domain.exception.WorkoutNotFoundException;
import com.n1b3lung0.gymrat.domain.repository.WorkoutRepositoryPort;

import java.util.Objects;

/**
 * Handles the {@link DeleteWorkoutCommand} use case.
 *
 * <ol>
 *   <li>Verifies the workout exists or throws {@link WorkoutNotFoundException}.
 *   <li>Delegates soft-delete to the repository port.
 *   <li>No domain event emitted (no aggregate method for deletion on Workout yet).
 * </ol>
 */
public class DeleteWorkoutHandler implements DeleteWorkoutUseCase {

    private final WorkoutRepositoryPort workoutRepository;
    private final DomainEventPublisherPort eventPublisher;

    public DeleteWorkoutHandler(
            WorkoutRepositoryPort workoutRepository,
            DomainEventPublisherPort eventPublisher) {
        this.workoutRepository = Objects.requireNonNull(workoutRepository);
        this.eventPublisher    = Objects.requireNonNull(eventPublisher);
    }

    @Override
    public void execute(DeleteWorkoutCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        if (workoutRepository.findById(command.id()).isEmpty()) {
            throw new WorkoutNotFoundException(command.id());
        }

        workoutRepository.deleteById(command.id());
    }
}


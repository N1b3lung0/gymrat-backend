package com.n1b3lung0.gymrat.application.command;

import com.n1b3lung0.gymrat.application.dto.FinishWorkoutCommand;
import com.n1b3lung0.gymrat.application.port.input.command.FinishWorkoutUseCase;
import com.n1b3lung0.gymrat.application.port.output.DomainEventPublisherPort;
import com.n1b3lung0.gymrat.domain.exception.WorkoutNotFoundException;
import com.n1b3lung0.gymrat.domain.repository.WorkoutRepositoryPort;

import java.util.Objects;

/**
 * Handles the {@link FinishWorkoutCommand} use case.
 *
 * <ol>
 *   <li>Loads the workout or throws {@link WorkoutNotFoundException}.
 *   <li>Calls {@code workout.finish(endWorkout)}.
 *   <li>Persists the updated aggregate.
 *   <li>Publishes accumulated domain events.
 * </ol>
 */
public class FinishWorkoutHandler implements FinishWorkoutUseCase {

    private final WorkoutRepositoryPort workoutRepository;
    private final DomainEventPublisherPort eventPublisher;

    public FinishWorkoutHandler(
            WorkoutRepositoryPort workoutRepository,
            DomainEventPublisherPort eventPublisher) {
        this.workoutRepository = Objects.requireNonNull(workoutRepository);
        this.eventPublisher    = Objects.requireNonNull(eventPublisher);
    }

    @Override
    public void execute(FinishWorkoutCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        var workout = workoutRepository.findById(command.id())
                .orElseThrow(() -> new WorkoutNotFoundException(command.id()));

        workout.finish(command.endWorkout());

        workoutRepository.save(workout);
        workout.pullDomainEvents().forEach(eventPublisher::publish);
    }
}


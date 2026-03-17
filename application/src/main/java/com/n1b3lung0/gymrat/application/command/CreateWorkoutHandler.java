package com.n1b3lung0.gymrat.application.command;

import com.n1b3lung0.gymrat.application.dto.CreateWorkoutCommand;
import com.n1b3lung0.gymrat.application.port.input.command.CreateWorkoutUseCase;
import com.n1b3lung0.gymrat.application.port.output.DomainEventPublisherPort;
import com.n1b3lung0.gymrat.application.port.output.MetricsPort;
import com.n1b3lung0.gymrat.domain.model.Workout;
import com.n1b3lung0.gymrat.domain.model.WorkoutId;
import com.n1b3lung0.gymrat.domain.repository.WorkoutRepositoryPort;

import java.util.Objects;

/**
 * Handles the {@link CreateWorkoutCommand} use case.
 *
 * <ol>
 *   <li>Creates the {@link Workout} aggregate via its factory method.
 *   <li>Persists the aggregate.
 *   <li>Publishes accumulated domain events.
 * </ol>
 */
public class CreateWorkoutHandler implements CreateWorkoutUseCase {

    static final String WORKOUTS_STARTED_METRIC = "workouts.started.total";

    private final WorkoutRepositoryPort    workoutRepository;
    private final DomainEventPublisherPort eventPublisher;
    private final MetricsPort              metrics;

    public CreateWorkoutHandler(
            WorkoutRepositoryPort workoutRepository,
            DomainEventPublisherPort eventPublisher,
            MetricsPort metrics) {
        this.workoutRepository = Objects.requireNonNull(workoutRepository);
        this.eventPublisher    = Objects.requireNonNull(eventPublisher);
        this.metrics           = Objects.requireNonNull(metrics);
    }

    @Override
    public WorkoutId execute(CreateWorkoutCommand command) {
        Objects.requireNonNull(command, "command must not be null");
        Objects.requireNonNull(command.startWorkout(), "startWorkout must not be null");

        var workout = Workout.create(command.startWorkout());

        workoutRepository.save(workout);
        workout.pullDomainEvents().forEach(eventPublisher::publish);

        metrics.increment(WORKOUTS_STARTED_METRIC);

        return workout.getId();
    }
}


package com.n1b3lung0.gymrat.infrastructure.config;

import com.n1b3lung0.gymrat.application.command.CreateWorkoutHandler;
import com.n1b3lung0.gymrat.application.command.DeleteWorkoutHandler;
import com.n1b3lung0.gymrat.application.command.FinishWorkoutHandler;
import com.n1b3lung0.gymrat.application.port.input.command.CreateWorkoutUseCase;
import com.n1b3lung0.gymrat.application.port.input.command.DeleteWorkoutUseCase;
import com.n1b3lung0.gymrat.application.port.input.command.FinishWorkoutUseCase;
import com.n1b3lung0.gymrat.application.port.input.query.GetWorkoutByIdUseCase;
import com.n1b3lung0.gymrat.application.port.input.query.ListWorkoutsUseCase;
import com.n1b3lung0.gymrat.application.port.output.DomainEventPublisherPort;
import com.n1b3lung0.gymrat.application.port.output.WorkoutQueryPort;
import com.n1b3lung0.gymrat.application.query.GetWorkoutByIdHandler;
import com.n1b3lung0.gymrat.application.query.ListWorkoutsHandler;
import com.n1b3lung0.gymrat.domain.repository.WorkoutRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration that wires Workout use case handlers as beans.
 *
 * <p>Handlers are instantiated with {@code new} — no {@code @Service} on handler classes.
 */
@Configuration
public class WorkoutConfig {

    @Bean
    public CreateWorkoutUseCase createWorkoutUseCase(
            WorkoutRepositoryPort workoutRepository,
            DomainEventPublisherPort eventPublisher) {
        return new CreateWorkoutHandler(workoutRepository, eventPublisher);
    }

    @Bean
    public FinishWorkoutUseCase finishWorkoutUseCase(
            WorkoutRepositoryPort workoutRepository,
            DomainEventPublisherPort eventPublisher) {
        return new FinishWorkoutHandler(workoutRepository, eventPublisher);
    }

    @Bean
    public DeleteWorkoutUseCase deleteWorkoutUseCase(
            WorkoutRepositoryPort workoutRepository,
            DomainEventPublisherPort eventPublisher) {
        return new DeleteWorkoutHandler(workoutRepository, eventPublisher);
    }

    @Bean
    public GetWorkoutByIdUseCase getWorkoutByIdUseCase(WorkoutQueryPort workoutQueryPort) {
        return new GetWorkoutByIdHandler(workoutQueryPort);
    }

    @Bean
    public ListWorkoutsUseCase listWorkoutsUseCase(WorkoutQueryPort workoutQueryPort) {
        return new ListWorkoutsHandler(workoutQueryPort);
    }
}


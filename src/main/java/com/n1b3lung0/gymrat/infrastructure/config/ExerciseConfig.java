package com.n1b3lung0.gymrat.infrastructure.config;

import com.n1b3lung0.gymrat.application.command.CreateExerciseHandler;
import com.n1b3lung0.gymrat.application.command.DeleteExerciseHandler;
import com.n1b3lung0.gymrat.application.command.UpdateExerciseHandler;
import com.n1b3lung0.gymrat.application.port.input.command.CreateExerciseUseCase;
import com.n1b3lung0.gymrat.application.port.input.command.DeleteExerciseUseCase;
import com.n1b3lung0.gymrat.application.port.input.command.UpdateExerciseUseCase;
import com.n1b3lung0.gymrat.application.port.input.query.GetExerciseByIdUseCase;
import com.n1b3lung0.gymrat.application.port.input.query.ListExercisesUseCase;
import com.n1b3lung0.gymrat.application.port.output.DomainEventPublisherPort;
import com.n1b3lung0.gymrat.application.port.output.MetricsPort;
import com.n1b3lung0.gymrat.application.query.GetExerciseByIdHandler;
import com.n1b3lung0.gymrat.application.query.ListExercisesHandler;
import com.n1b3lung0.gymrat.application.port.output.ExerciseQueryPort;
import com.n1b3lung0.gymrat.domain.repository.ExerciseRepositoryPort;
import com.n1b3lung0.gymrat.domain.repository.MediaRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration that wires Exercise use case handlers as beans.
 *
 * <p>Handlers are instantiated with {@code new} — no {@code @Service} on handler classes.
 * This keeps the application layer free of Spring annotations and makes
 * dependency wiring explicit and auditable in one place.
 */
@Configuration
public class ExerciseConfig {

    @Bean
    public CreateExerciseUseCase createExerciseUseCase(
            ExerciseRepositoryPort exerciseRepository,
            MediaRepositoryPort mediaRepository,
            DomainEventPublisherPort eventPublisher,
            MetricsPort metrics) {
        return new CreateExerciseHandler(exerciseRepository, mediaRepository, eventPublisher, metrics);
    }

    @Bean
    public UpdateExerciseUseCase updateExerciseUseCase(
            ExerciseRepositoryPort exerciseRepository,
            MediaRepositoryPort mediaRepository,
            DomainEventPublisherPort eventPublisher) {
        return new UpdateExerciseHandler(exerciseRepository, mediaRepository, eventPublisher);
    }

    @Bean
    public DeleteExerciseUseCase deleteExerciseUseCase(
            ExerciseRepositoryPort exerciseRepository,
            DomainEventPublisherPort eventPublisher) {
        return new DeleteExerciseHandler(exerciseRepository, eventPublisher);
    }

    @Bean
    public GetExerciseByIdUseCase getExerciseByIdUseCase(ExerciseQueryPort exerciseQueryPort) {
        return new GetExerciseByIdHandler(exerciseQueryPort);
    }

    @Bean
    public ListExercisesUseCase listExercisesUseCase(ExerciseQueryPort exerciseQueryPort) {
        return new ListExercisesHandler(exerciseQueryPort);
    }
}


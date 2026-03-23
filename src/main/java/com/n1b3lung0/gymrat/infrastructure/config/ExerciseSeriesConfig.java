package com.n1b3lung0.gymrat.infrastructure.config;

import com.n1b3lung0.gymrat.application.command.AddExerciseToWorkoutHandler;
import com.n1b3lung0.gymrat.application.command.RemoveExerciseFromWorkoutHandler;
import com.n1b3lung0.gymrat.application.port.input.command.AddExerciseToWorkoutUseCase;
import com.n1b3lung0.gymrat.application.port.input.command.RemoveExerciseFromWorkoutUseCase;
import com.n1b3lung0.gymrat.application.port.input.query.GetExerciseSeriesByIdUseCase;
import com.n1b3lung0.gymrat.application.port.input.query.ListExerciseSeriesByWorkoutUseCase;
import com.n1b3lung0.gymrat.application.port.output.DomainEventPublisherPort;
import com.n1b3lung0.gymrat.application.port.output.ExerciseSeriesQueryPort;
import com.n1b3lung0.gymrat.application.query.GetExerciseSeriesByIdHandler;
import com.n1b3lung0.gymrat.application.query.ListExerciseSeriesByWorkoutHandler;
import com.n1b3lung0.gymrat.domain.repository.ExerciseRepositoryPort;
import com.n1b3lung0.gymrat.domain.repository.ExerciseSeriesRepositoryPort;
import com.n1b3lung0.gymrat.domain.repository.WorkoutRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration that wires ExerciseSeries use case handlers as beans.
 *
 * <p>Handlers are instantiated with {@code new} — no {@code @Service} on handler classes.
 */
@Configuration
public class ExerciseSeriesConfig {

    @Bean
    public AddExerciseToWorkoutUseCase addExerciseToWorkoutUseCase(
            WorkoutRepositoryPort workoutRepository,
            ExerciseRepositoryPort exerciseRepository,
            ExerciseSeriesRepositoryPort exerciseSeriesRepository,
            DomainEventPublisherPort eventPublisher) {
        return new AddExerciseToWorkoutHandler(
                workoutRepository, exerciseRepository, exerciseSeriesRepository, eventPublisher);
    }

    @Bean
    public RemoveExerciseFromWorkoutUseCase removeExerciseFromWorkoutUseCase(
            ExerciseSeriesRepositoryPort exerciseSeriesRepository) {
        return new RemoveExerciseFromWorkoutHandler(exerciseSeriesRepository);
    }

    @Bean
    public GetExerciseSeriesByIdUseCase getExerciseSeriesByIdUseCase(
            ExerciseSeriesQueryPort exerciseSeriesQueryPort) {
        return new GetExerciseSeriesByIdHandler(exerciseSeriesQueryPort);
    }

    @Bean
    public ListExerciseSeriesByWorkoutUseCase listExerciseSeriesByWorkoutUseCase(
            ExerciseSeriesQueryPort exerciseSeriesQueryPort) {
        return new ListExerciseSeriesByWorkoutHandler(exerciseSeriesQueryPort);
    }
}


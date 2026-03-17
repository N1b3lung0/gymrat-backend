package com.n1b3lung0.gymrat.infrastructure.config;

import com.n1b3lung0.gymrat.application.command.DeleteSeriesHandler;
import com.n1b3lung0.gymrat.application.command.RecordSeriesHandler;
import com.n1b3lung0.gymrat.application.command.UpdateSeriesHandler;
import com.n1b3lung0.gymrat.application.port.input.command.DeleteSeriesUseCase;
import com.n1b3lung0.gymrat.application.port.input.command.RecordSeriesUseCase;
import com.n1b3lung0.gymrat.application.port.input.command.UpdateSeriesUseCase;
import com.n1b3lung0.gymrat.application.port.input.query.GetSeriesByIdUseCase;
import com.n1b3lung0.gymrat.application.port.input.query.ListSeriesByExerciseSeriesUseCase;
import com.n1b3lung0.gymrat.application.port.output.DomainEventPublisherPort;
import com.n1b3lung0.gymrat.application.port.output.MetricsPort;
import com.n1b3lung0.gymrat.application.port.output.SeriesQueryPort;
import com.n1b3lung0.gymrat.application.query.GetSeriesByIdHandler;
import com.n1b3lung0.gymrat.application.query.ListSeriesByExerciseSeriesHandler;
import com.n1b3lung0.gymrat.domain.repository.ExerciseSeriesRepositoryPort;
import com.n1b3lung0.gymrat.domain.repository.SeriesRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration that wires Series use case handlers as beans.
 *
 * <p>Handlers are instantiated with {@code new} — no {@code @Service} on handler classes.
 */
@Configuration
public class SeriesConfig {

    @Bean
    public RecordSeriesUseCase recordSeriesUseCase(
            SeriesRepositoryPort seriesRepository,
            ExerciseSeriesRepositoryPort exerciseSeriesRepository,
            DomainEventPublisherPort eventPublisher,
            MetricsPort metrics) {
        return new RecordSeriesHandler(seriesRepository, exerciseSeriesRepository, eventPublisher, metrics);
    }

    @Bean
    public UpdateSeriesUseCase updateSeriesUseCase(
            SeriesRepositoryPort seriesRepository,
            DomainEventPublisherPort eventPublisher) {
        return new UpdateSeriesHandler(seriesRepository, eventPublisher);
    }

    @Bean
    public DeleteSeriesUseCase deleteSeriesUseCase(SeriesRepositoryPort seriesRepository) {
        return new DeleteSeriesHandler(seriesRepository);
    }

    @Bean
    public GetSeriesByIdUseCase getSeriesByIdUseCase(SeriesQueryPort seriesQueryPort) {
        return new GetSeriesByIdHandler(seriesQueryPort);
    }

    @Bean
    public ListSeriesByExerciseSeriesUseCase listSeriesByExerciseSeriesUseCase(
            SeriesQueryPort seriesQueryPort) {
        return new ListSeriesByExerciseSeriesHandler(seriesQueryPort);
    }
}


package com.n1b3lung0.gymrat.application.command;

import com.n1b3lung0.gymrat.application.dto.RecordSeriesCommand;
import com.n1b3lung0.gymrat.application.port.input.command.RecordSeriesUseCase;
import com.n1b3lung0.gymrat.application.port.output.DomainEventPublisherPort;
import com.n1b3lung0.gymrat.domain.exception.ExerciseSeriesNotFoundException;
import com.n1b3lung0.gymrat.domain.model.Series;
import com.n1b3lung0.gymrat.domain.model.SeriesId;
import com.n1b3lung0.gymrat.domain.repository.ExerciseSeriesRepositoryPort;
import com.n1b3lung0.gymrat.domain.repository.SeriesRepositoryPort;

import java.util.Objects;

/**
 * Handles the {@link RecordSeriesCommand} use case.
 *
 * <ol>
 *   <li>Verifies the parent {@code ExerciseSeries} exists.
 *   <li>Auto-computes the next {@code serialNumber} from the current count.
 *   <li>Creates the {@link Series} aggregate.
 *   <li>Registers the series reference on the {@code ExerciseSeries} aggregate.
 *   <li>Persists both aggregates.
 *   <li>Publishes accumulated domain events.
 * </ol>
 */
public class RecordSeriesHandler implements RecordSeriesUseCase {

    private final SeriesRepositoryPort          seriesRepository;
    private final ExerciseSeriesRepositoryPort  exerciseSeriesRepository;
    private final DomainEventPublisherPort      eventPublisher;

    public RecordSeriesHandler(
            SeriesRepositoryPort seriesRepository,
            ExerciseSeriesRepositoryPort exerciseSeriesRepository,
            DomainEventPublisherPort eventPublisher) {
        this.seriesRepository         = Objects.requireNonNull(seriesRepository);
        this.exerciseSeriesRepository = Objects.requireNonNull(exerciseSeriesRepository);
        this.eventPublisher           = Objects.requireNonNull(eventPublisher);
    }

    @Override
    public SeriesId execute(RecordSeriesCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        // 1. Verify parent ExerciseSeries exists
        var exerciseSeries = exerciseSeriesRepository.findById(command.exerciseSeriesId())
                .orElseThrow(() -> new ExerciseSeriesNotFoundException(command.exerciseSeriesId()));

        // 2. Auto-compute next serialNumber
        int nextSerialNumber =
                (int) seriesRepository.countByExerciseSeriesId(command.exerciseSeriesId()) + 1;

        // 3. Create Series aggregate
        var series = Series.create(
                nextSerialNumber,
                command.repetitionsToDo(),
                command.intensity(),
                command.weight(),
                command.restTime(),
                command.exerciseSeriesId()
        );

        // 4. Register reference on ExerciseSeries
        exerciseSeries.addSeries(series.getId());

        // 5. Persist both aggregates
        seriesRepository.save(series);
        exerciseSeriesRepository.save(exerciseSeries);

        // 6. Publish domain events
        series.pullDomainEvents().forEach(eventPublisher::publish);
        exerciseSeries.pullDomainEvents().forEach(eventPublisher::publish);

        return series.getId();
    }
}


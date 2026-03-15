package com.n1b3lung0.gymrat.application.command;

import com.n1b3lung0.gymrat.application.dto.UpdateSeriesCommand;
import com.n1b3lung0.gymrat.application.port.input.command.UpdateSeriesUseCase;
import com.n1b3lung0.gymrat.application.port.output.DomainEventPublisherPort;
import com.n1b3lung0.gymrat.domain.exception.SeriesNotFoundException;
import com.n1b3lung0.gymrat.domain.repository.SeriesRepositoryPort;

import java.util.Objects;

/**
 * Handles the {@link UpdateSeriesCommand} use case.
 *
 * <ol>
 *   <li>Loads the existing {@code Series} or throws {@link SeriesNotFoundException}.
 *   <li>Calls {@code series.update(...)} to apply changes.
 *   <li>Persists the aggregate.
 * </ol>
 */
public class UpdateSeriesHandler implements UpdateSeriesUseCase {

    private final SeriesRepositoryPort     seriesRepository;
    private final DomainEventPublisherPort eventPublisher;

    public UpdateSeriesHandler(
            SeriesRepositoryPort seriesRepository,
            DomainEventPublisherPort eventPublisher) {
        this.seriesRepository = Objects.requireNonNull(seriesRepository);
        this.eventPublisher   = Objects.requireNonNull(eventPublisher);
    }

    @Override
    public void execute(UpdateSeriesCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        var series = seriesRepository.findById(command.id())
                .orElseThrow(() -> new SeriesNotFoundException(command.id()));

        series.update(
                command.repetitionsToDo(),
                command.repetitionsDone(),
                command.intensity(),
                command.weight(),
                command.startSeries(),
                command.endSeries(),
                command.restTime()
        );

        seriesRepository.save(series);
        series.pullDomainEvents().forEach(eventPublisher::publish);
    }
}


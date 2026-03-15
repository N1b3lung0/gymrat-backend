package com.n1b3lung0.gymrat.application.command;

import com.n1b3lung0.gymrat.application.dto.DeleteSeriesCommand;
import com.n1b3lung0.gymrat.application.port.input.command.DeleteSeriesUseCase;
import com.n1b3lung0.gymrat.domain.exception.SeriesNotFoundException;
import com.n1b3lung0.gymrat.domain.repository.SeriesRepositoryPort;

import java.util.Objects;

/**
 * Handles the {@link DeleteSeriesCommand} use case.
 *
 * <ol>
 *   <li>Verifies the series exists or throws {@link SeriesNotFoundException}.
 *   <li>Delegates soft-delete to the repository port.
 * </ol>
 */
public class DeleteSeriesHandler implements DeleteSeriesUseCase {

    private final SeriesRepositoryPort seriesRepository;

    public DeleteSeriesHandler(SeriesRepositoryPort seriesRepository) {
        this.seriesRepository = Objects.requireNonNull(seriesRepository);
    }

    @Override
    public void execute(DeleteSeriesCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        if (seriesRepository.findById(command.id()).isEmpty()) {
            throw new SeriesNotFoundException(command.id());
        }

        seriesRepository.deleteById(command.id());
    }
}


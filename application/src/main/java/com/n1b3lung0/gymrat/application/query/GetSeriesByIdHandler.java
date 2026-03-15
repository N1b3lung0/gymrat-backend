package com.n1b3lung0.gymrat.application.query;

import com.n1b3lung0.gymrat.application.dto.GetSeriesByIdQuery;
import com.n1b3lung0.gymrat.application.dto.SeriesDetailView;
import com.n1b3lung0.gymrat.application.port.input.query.GetSeriesByIdUseCase;
import com.n1b3lung0.gymrat.application.port.output.SeriesQueryPort;
import com.n1b3lung0.gymrat.domain.exception.SeriesNotFoundException;

import java.util.Objects;

/**
 * Handles the {@link GetSeriesByIdQuery} use case.
 * Delegates to {@link SeriesQueryPort} — no aggregate loading (CQRS query side).
 */
public class GetSeriesByIdHandler implements GetSeriesByIdUseCase {

    private final SeriesQueryPort seriesQueryPort;

    public GetSeriesByIdHandler(SeriesQueryPort seriesQueryPort) {
        this.seriesQueryPort = Objects.requireNonNull(seriesQueryPort);
    }

    @Override
    public SeriesDetailView execute(GetSeriesByIdQuery query) {
        Objects.requireNonNull(query, "query must not be null");

        return seriesQueryPort.findDetailById(query.id())
                .orElseThrow(() -> new SeriesNotFoundException(query.id()));
    }
}


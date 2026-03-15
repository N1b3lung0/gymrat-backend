package com.n1b3lung0.gymrat.application.query;

import com.n1b3lung0.gymrat.application.dto.ListSeriesByExerciseSeriesQuery;
import com.n1b3lung0.gymrat.application.dto.SeriesSummaryView;
import com.n1b3lung0.gymrat.application.port.input.query.ListSeriesByExerciseSeriesUseCase;
import com.n1b3lung0.gymrat.application.port.output.SeriesQueryPort;

import java.util.List;
import java.util.Objects;

/**
 * Handles the {@link ListSeriesByExerciseSeriesQuery} use case.
 * Delegates to {@link SeriesQueryPort} — no aggregate loading (CQRS query side).
 */
public class ListSeriesByExerciseSeriesHandler implements ListSeriesByExerciseSeriesUseCase {

    private final SeriesQueryPort seriesQueryPort;

    public ListSeriesByExerciseSeriesHandler(SeriesQueryPort seriesQueryPort) {
        this.seriesQueryPort = Objects.requireNonNull(seriesQueryPort);
    }

    @Override
    public List<SeriesSummaryView> execute(ListSeriesByExerciseSeriesQuery query) {
        Objects.requireNonNull(query, "query must not be null");

        return seriesQueryPort.findAllSummariesByExerciseSeriesId(query.exerciseSeriesId());
    }
}


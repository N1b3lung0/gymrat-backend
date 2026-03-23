package com.n1b3lung0.gymrat.application.port.input.query;

import com.n1b3lung0.gymrat.application.dto.ListSeriesByExerciseSeriesQuery;
import com.n1b3lung0.gymrat.application.dto.SeriesSummaryView;

import java.util.List;

/** Input port — use case for listing all series sets within an exercise-series. */
public interface ListSeriesByExerciseSeriesUseCase {
    List<SeriesSummaryView> execute(ListSeriesByExerciseSeriesQuery query);
}


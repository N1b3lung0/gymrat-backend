package com.n1b3lung0.gymrat.application.port.input.query;

import com.n1b3lung0.gymrat.application.dto.ExerciseSeriesDetailView;
import com.n1b3lung0.gymrat.application.dto.GetExerciseSeriesByIdQuery;

/** Input port — use case for retrieving the full detail of a single exercise-series. */
public interface GetExerciseSeriesByIdUseCase {
    ExerciseSeriesDetailView execute(GetExerciseSeriesByIdQuery query);
}


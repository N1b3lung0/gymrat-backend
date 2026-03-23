package com.n1b3lung0.gymrat.application.query;

import com.n1b3lung0.gymrat.application.dto.ExerciseSeriesDetailView;
import com.n1b3lung0.gymrat.application.dto.GetExerciseSeriesByIdQuery;
import com.n1b3lung0.gymrat.application.port.input.query.GetExerciseSeriesByIdUseCase;
import com.n1b3lung0.gymrat.application.port.output.ExerciseSeriesQueryPort;
import com.n1b3lung0.gymrat.domain.exception.ExerciseSeriesNotFoundException;

import java.util.Objects;

/**
 * Handles the {@link GetExerciseSeriesByIdQuery} use case.
 * Delegates to {@link ExerciseSeriesQueryPort} — no aggregate loading (CQRS query side).
 */
public class GetExerciseSeriesByIdHandler implements GetExerciseSeriesByIdUseCase {

    private final ExerciseSeriesQueryPort exerciseSeriesQueryPort;

    public GetExerciseSeriesByIdHandler(ExerciseSeriesQueryPort exerciseSeriesQueryPort) {
        this.exerciseSeriesQueryPort = Objects.requireNonNull(exerciseSeriesQueryPort);
    }

    @Override
    public ExerciseSeriesDetailView execute(GetExerciseSeriesByIdQuery query) {
        Objects.requireNonNull(query, "query must not be null");

        return exerciseSeriesQueryPort.findDetailById(query.id())
                .orElseThrow(() -> new ExerciseSeriesNotFoundException(query.id()));
    }
}


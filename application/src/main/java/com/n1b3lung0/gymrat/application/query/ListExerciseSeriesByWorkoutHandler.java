package com.n1b3lung0.gymrat.application.query;

import com.n1b3lung0.gymrat.application.dto.ExerciseSeriesSummaryView;
import com.n1b3lung0.gymrat.application.dto.ListExerciseSeriesByWorkoutQuery;
import com.n1b3lung0.gymrat.application.port.input.query.ListExerciseSeriesByWorkoutUseCase;
import com.n1b3lung0.gymrat.application.port.output.ExerciseSeriesQueryPort;

import java.util.List;
import java.util.Objects;

/**
 * Handles the {@link ListExerciseSeriesByWorkoutQuery} use case.
 * Delegates to {@link ExerciseSeriesQueryPort} — no aggregate loading (CQRS query side).
 */
public class ListExerciseSeriesByWorkoutHandler implements ListExerciseSeriesByWorkoutUseCase {

    private final ExerciseSeriesQueryPort exerciseSeriesQueryPort;

    public ListExerciseSeriesByWorkoutHandler(ExerciseSeriesQueryPort exerciseSeriesQueryPort) {
        this.exerciseSeriesQueryPort = Objects.requireNonNull(exerciseSeriesQueryPort);
    }

    @Override
    public List<ExerciseSeriesSummaryView> execute(ListExerciseSeriesByWorkoutQuery query) {
        Objects.requireNonNull(query, "query must not be null");

        return exerciseSeriesQueryPort.findAllSummariesByWorkoutId(query.workoutId());
    }
}


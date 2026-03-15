package com.n1b3lung0.gymrat.application.query;

import com.n1b3lung0.gymrat.application.dto.ExerciseSummaryView;
import com.n1b3lung0.gymrat.application.dto.ListExercisesQuery;
import com.n1b3lung0.gymrat.application.port.input.query.ListExercisesUseCase;
import com.n1b3lung0.gymrat.application.port.output.ExerciseQueryPort;
import com.n1b3lung0.gymrat.domain.repository.PageResult;

import java.util.Objects;

/**
 * Handles the {@link ListExercisesQuery} use case.
 *
 * <p>Delegates directly to the {@link ExerciseQueryPort} read-model projection —
 * no aggregate loading (CQRS query side).
 */
public class ListExercisesHandler implements ListExercisesUseCase {

    private final ExerciseQueryPort exerciseQueryPort;

    public ListExercisesHandler(ExerciseQueryPort exerciseQueryPort) {
        this.exerciseQueryPort = Objects.requireNonNull(exerciseQueryPort);
    }

    @Override
    public PageResult<ExerciseSummaryView> execute(ListExercisesQuery query) {
        Objects.requireNonNull(query, "query must not be null");

        return exerciseQueryPort.findAll(query.pageRequest());
    }
}


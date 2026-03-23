package com.n1b3lung0.gymrat.application.query;

import com.n1b3lung0.gymrat.application.dto.ListWorkoutsQuery;
import com.n1b3lung0.gymrat.application.dto.WorkoutSummaryView;
import com.n1b3lung0.gymrat.application.port.input.query.ListWorkoutsUseCase;
import com.n1b3lung0.gymrat.application.port.output.WorkoutQueryPort;
import com.n1b3lung0.gymrat.domain.repository.PageResult;

import java.util.Objects;

/**
 * Handles the {@link ListWorkoutsQuery} use case.
 * Delegates to {@link WorkoutQueryPort} — no aggregate loading (CQRS query side).
 */
public class ListWorkoutsHandler implements ListWorkoutsUseCase {

    private final WorkoutQueryPort workoutQueryPort;

    public ListWorkoutsHandler(WorkoutQueryPort workoutQueryPort) {
        this.workoutQueryPort = Objects.requireNonNull(workoutQueryPort);
    }

    @Override
    public PageResult<WorkoutSummaryView> execute(ListWorkoutsQuery query) {
        Objects.requireNonNull(query, "query must not be null");

        return workoutQueryPort.findAllSummaries(query.pageRequest());
    }
}


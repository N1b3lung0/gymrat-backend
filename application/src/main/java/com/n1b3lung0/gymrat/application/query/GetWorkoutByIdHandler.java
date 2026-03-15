package com.n1b3lung0.gymrat.application.query;

import com.n1b3lung0.gymrat.application.dto.GetWorkoutByIdQuery;
import com.n1b3lung0.gymrat.application.dto.WorkoutDetailView;
import com.n1b3lung0.gymrat.application.port.input.query.GetWorkoutByIdUseCase;
import com.n1b3lung0.gymrat.application.port.output.WorkoutQueryPort;
import com.n1b3lung0.gymrat.domain.exception.WorkoutNotFoundException;

import java.util.Objects;

/**
 * Handles the {@link GetWorkoutByIdQuery} use case.
 * Delegates to {@link WorkoutQueryPort} — no aggregate loading (CQRS query side).
 */
public class GetWorkoutByIdHandler implements GetWorkoutByIdUseCase {

    private final WorkoutQueryPort workoutQueryPort;

    public GetWorkoutByIdHandler(WorkoutQueryPort workoutQueryPort) {
        this.workoutQueryPort = Objects.requireNonNull(workoutQueryPort);
    }

    @Override
    public WorkoutDetailView execute(GetWorkoutByIdQuery query) {
        Objects.requireNonNull(query, "query must not be null");

        return workoutQueryPort.findDetailById(query.id())
                .orElseThrow(() -> new WorkoutNotFoundException(query.id()));
    }
}


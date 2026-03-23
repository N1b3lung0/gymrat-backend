package com.n1b3lung0.gymrat.application.query;

import com.n1b3lung0.gymrat.application.dto.ExerciseDetailView;
import com.n1b3lung0.gymrat.application.dto.GetExerciseByIdQuery;
import com.n1b3lung0.gymrat.application.port.input.query.GetExerciseByIdUseCase;
import com.n1b3lung0.gymrat.application.port.output.ExerciseQueryPort;
import com.n1b3lung0.gymrat.domain.exception.ExerciseNotFoundException;

import java.util.Objects;

/**
 * Handles the {@link GetExerciseByIdQuery} use case.
 *
 * <p>Delegates directly to the {@link ExerciseQueryPort} read-model projection —
 * no aggregate loading (CQRS query side).
 */
public class GetExerciseByIdHandler implements GetExerciseByIdUseCase {

    private final ExerciseQueryPort exerciseQueryPort;

    public GetExerciseByIdHandler(ExerciseQueryPort exerciseQueryPort) {
        this.exerciseQueryPort = Objects.requireNonNull(exerciseQueryPort);
    }

    @Override
    public ExerciseDetailView execute(GetExerciseByIdQuery query) {
        Objects.requireNonNull(query, "query must not be null");

        return exerciseQueryPort.findDetailById(query.id())
                .orElseThrow(() -> new ExerciseNotFoundException(query.id()));
    }
}


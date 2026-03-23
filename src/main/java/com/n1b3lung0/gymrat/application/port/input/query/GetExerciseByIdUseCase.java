package com.n1b3lung0.gymrat.application.port.input.query;

import com.n1b3lung0.gymrat.application.dto.ExerciseDetailView;
import com.n1b3lung0.gymrat.application.dto.GetExerciseByIdQuery;

/**
 * Input port — use case for retrieving the full detail of a single exercise.
 */
public interface GetExerciseByIdUseCase {

    /**
     * Executes the get exercise by id use case.
     *
     * @param query the query carrying the exercise id
     * @return the full {@link ExerciseDetailView}
     * @throws com.n1b3lung0.gymrat.domain.exception.ExerciseNotFoundException if not found
     */
    ExerciseDetailView execute(GetExerciseByIdQuery query);
}


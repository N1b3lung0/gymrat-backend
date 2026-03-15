package com.n1b3lung0.gymrat.application.port.input.query;

import com.n1b3lung0.gymrat.application.dto.GetWorkoutByIdQuery;
import com.n1b3lung0.gymrat.application.dto.WorkoutDetailView;

/** Input port — use case for retrieving the full detail of a single workout. */
public interface GetWorkoutByIdUseCase {
    WorkoutDetailView execute(GetWorkoutByIdQuery query);
}


package com.n1b3lung0.gymrat.application.port.input.command;

import com.n1b3lung0.gymrat.application.dto.CreateWorkoutCommand;
import com.n1b3lung0.gymrat.domain.model.WorkoutId;

/** Input port — use case for starting a new workout session. */
public interface CreateWorkoutUseCase {
    WorkoutId execute(CreateWorkoutCommand command);
}


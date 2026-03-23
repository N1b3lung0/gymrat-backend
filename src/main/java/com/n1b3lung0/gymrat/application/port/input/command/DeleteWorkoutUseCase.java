package com.n1b3lung0.gymrat.application.port.input.command;

import com.n1b3lung0.gymrat.application.dto.DeleteWorkoutCommand;

/** Input port — use case for soft-deleting a workout. */
public interface DeleteWorkoutUseCase {
    void execute(DeleteWorkoutCommand command);
}


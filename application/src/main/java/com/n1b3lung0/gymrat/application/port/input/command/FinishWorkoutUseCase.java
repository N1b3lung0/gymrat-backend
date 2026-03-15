package com.n1b3lung0.gymrat.application.port.input.command;

import com.n1b3lung0.gymrat.application.dto.FinishWorkoutCommand;

/** Input port — use case for finishing an open workout session. */
public interface FinishWorkoutUseCase {
    void execute(FinishWorkoutCommand command);
}


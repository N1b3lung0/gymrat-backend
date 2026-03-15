package com.n1b3lung0.gymrat.application.port.input.command;

import com.n1b3lung0.gymrat.application.dto.DeleteSeriesCommand;

/** Input port — use case for soft-deleting a series set. */
public interface DeleteSeriesUseCase {
    void execute(DeleteSeriesCommand command);
}


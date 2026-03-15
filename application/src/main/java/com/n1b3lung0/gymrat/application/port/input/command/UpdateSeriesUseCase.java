package com.n1b3lung0.gymrat.application.port.input.command;

import com.n1b3lung0.gymrat.application.dto.UpdateSeriesCommand;

/** Input port — use case for updating an existing series set. */
public interface UpdateSeriesUseCase {
    void execute(UpdateSeriesCommand command);
}


package com.n1b3lung0.gymrat.application.port.input.command;

import com.n1b3lung0.gymrat.application.dto.RecordSeriesCommand;
import com.n1b3lung0.gymrat.domain.model.SeriesId;

/** Input port — use case for recording a new series set within an exercise-series. */
public interface RecordSeriesUseCase {
    SeriesId execute(RecordSeriesCommand command);
}


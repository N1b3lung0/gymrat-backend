package com.n1b3lung0.gymrat.application.port.input.query;

import com.n1b3lung0.gymrat.application.dto.GetSeriesByIdQuery;
import com.n1b3lung0.gymrat.application.dto.SeriesDetailView;

/** Input port — use case for retrieving the full detail of a single series set. */
public interface GetSeriesByIdUseCase {
    SeriesDetailView execute(GetSeriesByIdQuery query);
}


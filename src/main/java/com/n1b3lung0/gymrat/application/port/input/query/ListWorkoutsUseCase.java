package com.n1b3lung0.gymrat.application.port.input.query;

import com.n1b3lung0.gymrat.application.dto.ListWorkoutsQuery;
import com.n1b3lung0.gymrat.application.dto.WorkoutSummaryView;
import com.n1b3lung0.gymrat.domain.repository.PageResult;

/** Input port — use case for retrieving a paginated list of workout summaries. */
public interface ListWorkoutsUseCase {
    PageResult<WorkoutSummaryView> execute(ListWorkoutsQuery query);
}


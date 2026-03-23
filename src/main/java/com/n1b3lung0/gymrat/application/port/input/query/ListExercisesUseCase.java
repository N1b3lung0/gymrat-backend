package com.n1b3lung0.gymrat.application.port.input.query;

import com.n1b3lung0.gymrat.application.dto.ExerciseSummaryView;
import com.n1b3lung0.gymrat.application.dto.ListExercisesQuery;
import com.n1b3lung0.gymrat.domain.repository.PageResult;

/**
 * Input port — use case for retrieving a paginated list of exercise summaries.
 */
public interface ListExercisesUseCase {

    /**
     * Executes the list exercises use case.
     *
     * @param query the query carrying pagination parameters
     * @return a paginated {@link PageResult} of {@link ExerciseSummaryView}
     */
    PageResult<ExerciseSummaryView> execute(ListExercisesQuery query);
}


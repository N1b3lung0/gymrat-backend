package com.n1b3lung0.gymrat.application.port.output;

import com.n1b3lung0.gymrat.application.dto.ExerciseDetailView;
import com.n1b3lung0.gymrat.application.dto.ExerciseSummaryView;
import com.n1b3lung0.gymrat.domain.model.ExerciseId;
import com.n1b3lung0.gymrat.domain.repository.PageRequest;
import com.n1b3lung0.gymrat.domain.repository.PageResult;

import java.util.Optional;

/**
 * Output port for exercise read-model queries (CQRS query side).
 *
 * <p>Implementations bypass the domain aggregate and query persistence
 * projections directly for optimal read performance.
 */
public interface ExerciseQueryPort {

    /**
     * Returns the full detail view of the exercise with the given identifier.
     *
     * @param id the exercise identifier
     * @return an {@link Optional} containing the detail view, or empty if not found
     */
    Optional<ExerciseDetailView> findDetailById(ExerciseId id);

    /**
     * Returns a paginated list of exercise summaries.
     *
     * @param pageRequest pagination and sorting parameters
     * @return a {@link PageResult} of {@link ExerciseSummaryView}
     */
    PageResult<ExerciseSummaryView> findAllSummaries(PageRequest pageRequest);
}


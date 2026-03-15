package com.n1b3lung0.gymrat.application.port.output;

import com.n1b3lung0.gymrat.application.dto.WorkoutDetailView;
import com.n1b3lung0.gymrat.application.dto.WorkoutSummaryView;
import com.n1b3lung0.gymrat.domain.model.WorkoutId;
import com.n1b3lung0.gymrat.domain.repository.PageRequest;
import com.n1b3lung0.gymrat.domain.repository.PageResult;

import java.util.Optional;

/**
 * Output port for workout read-model queries (CQRS query side).
 *
 * <p>Implementations bypass the domain aggregate and query persistence
 * projections directly for optimal read performance.
 */
public interface WorkoutQueryPort {

    /**
     * Returns the full detail view of the workout with the given identifier.
     *
     * @param id the workout identifier
     * @return an {@link Optional} containing the detail view, or empty if not found
     */
    Optional<WorkoutDetailView> findDetailById(WorkoutId id);

    /**
     * Returns a paginated list of workout summaries.
     *
     * @param pageRequest pagination and sorting parameters
     * @return a {@link PageResult} of {@link WorkoutSummaryView}
     */
    PageResult<WorkoutSummaryView> findAll(PageRequest pageRequest);
}


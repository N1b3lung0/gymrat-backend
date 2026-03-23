package com.n1b3lung0.gymrat.application.port.output;

import com.n1b3lung0.gymrat.application.dto.SeriesDetailView;
import com.n1b3lung0.gymrat.application.dto.SeriesSummaryView;
import com.n1b3lung0.gymrat.domain.model.ExerciseSeriesId;
import com.n1b3lung0.gymrat.domain.model.SeriesId;

import java.util.List;
import java.util.Optional;

/**
 * Output port for {@code Series} read-model queries (CQRS query side).
 */
public interface SeriesQueryPort {

    /**
     * Returns the full detail view of the series with the given identifier.
     *
     * @param id the series identifier
     * @return an {@link Optional} containing the detail view, or empty if not found
     */
    Optional<SeriesDetailView> findDetailById(SeriesId id);

    /**
     * Returns all series summaries belonging to the given exercise-series,
     * ordered by {@code serialNumber} ascending.
     *
     * @param exerciseSeriesId the parent exercise-series identifier
     * @return ordered list of summaries; never {@code null}
     */
    List<SeriesSummaryView> findAllSummariesByExerciseSeriesId(ExerciseSeriesId exerciseSeriesId);
}


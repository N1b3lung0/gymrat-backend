package com.n1b3lung0.gymrat.application.port.output;

import com.n1b3lung0.gymrat.application.dto.ExerciseSeriesDetailView;
import com.n1b3lung0.gymrat.application.dto.ExerciseSeriesSummaryView;
import com.n1b3lung0.gymrat.domain.model.ExerciseSeriesId;
import com.n1b3lung0.gymrat.domain.model.WorkoutId;

import java.util.List;
import java.util.Optional;

/**
 * Output port for {@code ExerciseSeries} read-model queries (CQRS query side).
 */
public interface ExerciseSeriesQueryPort {

    /**
     * Returns the full detail view of the exercise-series with the given identifier.
     *
     * @param id the exercise-series identifier
     * @return an {@link Optional} containing the detail view, or empty if not found
     */
    Optional<ExerciseSeriesDetailView> findDetailById(ExerciseSeriesId id);

    /**
     * Returns all exercise-series summaries belonging to the given workout.
     *
     * @param workoutId the parent workout identifier
     * @return list of summaries; never {@code null}
     */
    List<ExerciseSeriesSummaryView> findAllSummariesByWorkoutId(WorkoutId workoutId);
}


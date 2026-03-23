package com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.mapper;

import com.n1b3lung0.gymrat.application.dto.AddExerciseToWorkoutCommand;
import com.n1b3lung0.gymrat.application.dto.ExerciseSeriesDetailView;
import com.n1b3lung0.gymrat.application.dto.ExerciseSeriesSummaryView;
import com.n1b3lung0.gymrat.domain.model.ExerciseId;
import com.n1b3lung0.gymrat.domain.model.WorkoutId;
import com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto.AddExerciseToWorkoutRequest;
import com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto.ExerciseSeriesResponse;
import com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto.ExerciseSeriesSummaryResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Hand-written mapper between ExerciseSeries REST DTOs and application-layer commands/views.
 */
@Component
public class ExerciseSeriesRestMapper {

    // -------------------------------------------------------------------------
    // Request → Command
    // -------------------------------------------------------------------------

    /**
     * Maps an {@link AddExerciseToWorkoutRequest} to an {@link AddExerciseToWorkoutCommand}.
     *
     * @param workoutId the parent workout UUID from the path variable; must not be {@code null}
     * @param request   the validated REST request; must not be {@code null}
     * @return the application command
     */
    public AddExerciseToWorkoutCommand toAddCommand(UUID workoutId, AddExerciseToWorkoutRequest request) {
        Objects.requireNonNull(workoutId, "workoutId must not be null");
        Objects.requireNonNull(request, "AddExerciseToWorkoutRequest must not be null");
        return new AddExerciseToWorkoutCommand(
                WorkoutId.of(workoutId),
                ExerciseId.of(request.exerciseId())
        );
    }

    // -------------------------------------------------------------------------
    // View → Response
    // -------------------------------------------------------------------------

    /**
     * Maps an {@link ExerciseSeriesDetailView} to an {@link ExerciseSeriesResponse}.
     *
     * @param view the application read model; must not be {@code null}
     * @return the REST response DTO
     */
    public ExerciseSeriesResponse toResponse(ExerciseSeriesDetailView view) {
        Objects.requireNonNull(view, "ExerciseSeriesDetailView must not be null");
        return new ExerciseSeriesResponse(
                view.id(),
                view.workoutId(),
                view.exerciseId(),
                view.seriesIds()
        );
    }

    /**
     * Maps an {@link ExerciseSeriesSummaryView} to an {@link ExerciseSeriesSummaryResponse}.
     *
     * @param view the application read model; must not be {@code null}
     * @return the lightweight REST response DTO
     */
    public ExerciseSeriesSummaryResponse toSummaryResponse(ExerciseSeriesSummaryView view) {
        Objects.requireNonNull(view, "ExerciseSeriesSummaryView must not be null");
        return new ExerciseSeriesSummaryResponse(
                view.id(),
                view.exerciseId(),
                view.seriesCount()
        );
    }

    /**
     * Maps a list of {@link ExerciseSeriesSummaryView} to a list of {@link ExerciseSeriesSummaryResponse}.
     *
     * @param views the application read models; must not be {@code null}
     * @return the list of lightweight REST response DTOs
     */
    public List<ExerciseSeriesSummaryResponse> toSummaryResponseList(List<ExerciseSeriesSummaryView> views) {
        Objects.requireNonNull(views, "ExerciseSeriesSummaryView list must not be null");
        return views.stream().map(this::toSummaryResponse).toList();
    }
}


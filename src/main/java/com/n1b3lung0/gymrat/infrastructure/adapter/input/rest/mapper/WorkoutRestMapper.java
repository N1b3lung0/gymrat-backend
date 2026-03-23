package com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.mapper;

import com.n1b3lung0.gymrat.application.dto.CreateWorkoutCommand;
import com.n1b3lung0.gymrat.application.dto.FinishWorkoutCommand;
import com.n1b3lung0.gymrat.application.dto.WorkoutDetailView;
import com.n1b3lung0.gymrat.application.dto.WorkoutSummaryView;
import com.n1b3lung0.gymrat.domain.model.WorkoutId;
import com.n1b3lung0.gymrat.domain.repository.PageResult;
import com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto.CreateWorkoutRequest;
import com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto.FinishWorkoutRequest;
import com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto.PageResponse;
import com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto.WorkoutResponse;
import com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto.WorkoutSummaryResponse;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

/**
 * Hand-written mapper between Workout REST DTOs and application-layer commands/views.
 *
 * <p>Belongs to the infrastructure REST adapter — never imported by domain or application.
 */
@Component
public class WorkoutRestMapper {

    // -------------------------------------------------------------------------
    // Request → Command
    // -------------------------------------------------------------------------

    /**
     * Maps a {@link CreateWorkoutRequest} to a {@link CreateWorkoutCommand}.
     *
     * @param request the validated REST request; must not be {@code null}
     * @return the application command
     */
    public CreateWorkoutCommand toCreateCommand(CreateWorkoutRequest request) {
        Objects.requireNonNull(request, "CreateWorkoutRequest must not be null");
        return new CreateWorkoutCommand(request.startWorkout());
    }

    /**
     * Maps a {@link FinishWorkoutRequest} to a {@link FinishWorkoutCommand}.
     *
     * @param id      the workout identifier from the path variable; must not be {@code null}
     * @param request the validated REST request; must not be {@code null}
     * @return the application command
     */
    public FinishWorkoutCommand toFinishCommand(UUID id, FinishWorkoutRequest request) {
        Objects.requireNonNull(id, "Workout id must not be null");
        Objects.requireNonNull(request, "FinishWorkoutRequest must not be null");
        return new FinishWorkoutCommand(WorkoutId.of(id), request.endWorkout());
    }

    // -------------------------------------------------------------------------
    // View → Response
    // -------------------------------------------------------------------------

    /**
     * Maps a {@link WorkoutDetailView} to a {@link WorkoutResponse}.
     *
     * @param view the application read model; must not be {@code null}
     * @return the REST response DTO
     */
    public WorkoutResponse toResponse(WorkoutDetailView view) {
        Objects.requireNonNull(view, "WorkoutDetailView must not be null");
        return new WorkoutResponse(
                view.id(),
                view.startWorkout(),
                view.endWorkout(),
                view.endWorkout() != null,
                view.exerciseSeriesIds()
        );
    }

    /**
     * Maps a {@link WorkoutSummaryView} to a {@link WorkoutSummaryResponse}.
     *
     * @param view the application read model; must not be {@code null}
     * @return the lightweight REST response DTO
     */
    public WorkoutSummaryResponse toSummaryResponse(WorkoutSummaryView view) {
        Objects.requireNonNull(view, "WorkoutSummaryView must not be null");
        return new WorkoutSummaryResponse(
                view.id(),
                view.startWorkout(),
                view.endWorkout(),
                view.finished()
        );
    }

    /**
     * Maps a {@link PageResult} of {@link WorkoutSummaryView} to a
     * {@link PageResponse} of {@link WorkoutSummaryResponse}.
     *
     * @param page the domain page result; must not be {@code null}
     * @return the paged REST envelope
     */
    public PageResponse<WorkoutSummaryResponse> toPageResponse(PageResult<WorkoutSummaryView> page) {
        Objects.requireNonNull(page, "PageResult must not be null");
        var content = page.content().stream().map(this::toSummaryResponse).toList();
        return new PageResponse<>(
                content,
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages(),
                page.isLast()
        );
    }
}


package com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.mapper;

import com.n1b3lung0.gymrat.application.dto.CreateExerciseCommand;
import com.n1b3lung0.gymrat.application.dto.ExerciseDetailView;
import com.n1b3lung0.gymrat.application.dto.ExerciseSummaryView;
import com.n1b3lung0.gymrat.application.dto.MediaView;
import com.n1b3lung0.gymrat.application.dto.UpdateExerciseCommand;
import com.n1b3lung0.gymrat.domain.model.ExerciseId;
import com.n1b3lung0.gymrat.domain.repository.PageResult;
import com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto.CreateExerciseRequest;
import com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto.ExerciseResponse;
import com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto.ExerciseSummaryResponse;
import com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto.MediaRequest;
import com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto.MediaResponse;
import com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto.PageResponse;
import com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto.UpdateExerciseRequest;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

/**
 * Hand-written mapper between Exercise REST DTOs and application-layer commands/views.
 *
 * <p>Belongs to the infrastructure REST adapter — never imported by domain or application.
 */
@Component
public class ExerciseRestMapper {

    // -------------------------------------------------------------------------
    // Request → Command
    // -------------------------------------------------------------------------

    /**
     * Maps a {@link CreateExerciseRequest} to a {@link CreateExerciseCommand}.
     *
     * @param request        the validated REST request; must not be {@code null}
     * @param idempotencyKey client-supplied key for idempotent creation; may be {@code null}
     * @return the application command
     */
    public CreateExerciseCommand toCreateCommand(CreateExerciseRequest request, UUID idempotencyKey) {
        Objects.requireNonNull(request, "CreateExerciseRequest must not be null");
        return new CreateExerciseCommand(
                request.name(),
                request.description(),
                request.level(),
                request.routines(),
                request.primaryMuscle(),
                request.secondaryMuscles(),
                request.image() != null ? request.image().name()        : null,
                request.image() != null ? request.image().description() : null,
                request.image() != null ? request.image().url()         : null,
                request.video() != null ? request.video().name()        : null,
                request.video() != null ? request.video().description() : null,
                request.video() != null ? request.video().url()         : null,
                idempotencyKey
        );
    }

    /**
     * Maps an {@link UpdateExerciseRequest} to an {@link UpdateExerciseCommand}.
     *
     * @param id      the exercise identifier from the path variable; must not be {@code null}
     * @param request the validated REST request; must not be {@code null}
     * @return the application command
     */
    public UpdateExerciseCommand toUpdateCommand(UUID id, UpdateExerciseRequest request) {
        Objects.requireNonNull(id, "Exercise id must not be null");
        Objects.requireNonNull(request, "UpdateExerciseRequest must not be null");
        return new UpdateExerciseCommand(
                ExerciseId.of(id),
                request.name(),
                request.description(),
                request.level(),
                request.routines(),
                request.primaryMuscle(),
                request.secondaryMuscles(),
                request.image() != null ? request.image().name()        : null,
                request.image() != null ? request.image().description() : null,
                request.image() != null ? request.image().url()         : null,
                request.video() != null ? request.video().name()        : null,
                request.video() != null ? request.video().description() : null,
                request.video() != null ? request.video().url()         : null
        );
    }

    // -------------------------------------------------------------------------
    // View → Response
    // -------------------------------------------------------------------------

    /**
     * Maps an {@link ExerciseDetailView} to an {@link ExerciseResponse}.
     *
     * @param view the application read model; must not be {@code null}
     * @return the REST response DTO
     */
    public ExerciseResponse toResponse(ExerciseDetailView view) {
        Objects.requireNonNull(view, "ExerciseDetailView must not be null");
        return new ExerciseResponse(
                view.id(),
                view.name(),
                view.description(),
                view.level(),
                view.routines(),
                view.primaryMuscle(),
                view.secondaryMuscles(),
                toMediaResponse(view.image()),
                toMediaResponse(view.video())
        );
    }

    /**
     * Maps an {@link ExerciseSummaryView} to an {@link ExerciseSummaryResponse}.
     *
     * @param view the application read model; must not be {@code null}
     * @return the lightweight REST response DTO
     */
    public ExerciseSummaryResponse toSummaryResponse(ExerciseSummaryView view) {
        Objects.requireNonNull(view, "ExerciseSummaryView must not be null");
        return new ExerciseSummaryResponse(
                view.id(),
                view.name(),
                view.level(),
                view.primaryMuscle(),
                view.routines()
        );
    }

    /**
     * Maps a {@link PageResult} of {@link ExerciseSummaryView} to a
     * {@link PageResponse} of {@link ExerciseSummaryResponse}.
     *
     * @param page the domain page result; must not be {@code null}
     * @return the paged REST envelope
     */
    public PageResponse<ExerciseSummaryResponse> toPageResponse(PageResult<ExerciseSummaryView> page) {
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

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private static MediaResponse toMediaResponse(MediaView view) {
        if (view == null) return null;
        return new MediaResponse(view.name(), view.description(), view.url());
    }

    /** Unused at this step but kept for symmetry — request → view direction if ever needed. */
    private static MediaView toMediaView(MediaRequest request) {
        if (request == null) return null;
        return new MediaView(request.name(), request.description(), request.url());
    }
}


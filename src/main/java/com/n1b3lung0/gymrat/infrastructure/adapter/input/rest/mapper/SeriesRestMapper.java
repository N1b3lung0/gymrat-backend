package com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.mapper;

import com.n1b3lung0.gymrat.application.dto.RecordSeriesCommand;
import com.n1b3lung0.gymrat.application.dto.SeriesDetailView;
import com.n1b3lung0.gymrat.application.dto.SeriesSummaryView;
import com.n1b3lung0.gymrat.application.dto.UpdateSeriesCommand;
import com.n1b3lung0.gymrat.domain.model.ExerciseSeriesId;
import com.n1b3lung0.gymrat.domain.model.SeriesId;
import com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto.RecordSeriesRequest;
import com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto.SeriesResponse;
import com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto.SeriesSummaryResponse;
import com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto.UpdateSeriesRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Hand-written mapper between Series REST DTOs and application-layer commands/views.
 *
 * <p>Belongs to the infrastructure REST adapter — never imported by domain or application.
 */
@Component
public class SeriesRestMapper {

    // -------------------------------------------------------------------------
    // Request → Command
    // -------------------------------------------------------------------------

    /**
     * Maps a {@link RecordSeriesRequest} to a {@link RecordSeriesCommand}.
     *
     * @param exerciseSeriesId the parent exercise-series UUID from the path variable
     * @param request          the validated REST request; must not be {@code null}
     * @return the application command
     */
    public RecordSeriesCommand toRecordCommand(UUID exerciseSeriesId, RecordSeriesRequest request) {
        Objects.requireNonNull(exerciseSeriesId, "exerciseSeriesId must not be null");
        Objects.requireNonNull(request, "RecordSeriesRequest must not be null");
        return new RecordSeriesCommand(
                ExerciseSeriesId.of(exerciseSeriesId),
                request.repetitionsToDo(),
                request.intensity(),
                request.weight(),
                request.restTime()
        );
    }

    /**
     * Maps an {@link UpdateSeriesRequest} to an {@link UpdateSeriesCommand}.
     *
     * @param seriesId the series UUID from the path variable; must not be {@code null}
     * @param request  the validated REST request; must not be {@code null}
     * @return the application command
     */
    public UpdateSeriesCommand toUpdateCommand(UUID seriesId, UpdateSeriesRequest request) {
        Objects.requireNonNull(seriesId, "seriesId must not be null");
        Objects.requireNonNull(request, "UpdateSeriesRequest must not be null");
        return new UpdateSeriesCommand(
                SeriesId.of(seriesId),
                request.repetitionsToDo(),
                request.repetitionsDone(),
                request.intensity(),
                request.weight(),
                request.startSeries(),
                request.endSeries(),
                request.restTime()
        );
    }

    // -------------------------------------------------------------------------
    // View → Response
    // -------------------------------------------------------------------------

    /**
     * Maps a {@link SeriesDetailView} to a {@link SeriesResponse}.
     *
     * @param view the application read model; must not be {@code null}
     * @return the REST response DTO
     */
    public SeriesResponse toResponse(SeriesDetailView view) {
        Objects.requireNonNull(view, "SeriesDetailView must not be null");
        return new SeriesResponse(
                view.id(),
                view.serialNumber(),
                view.repetitionsToDo(),
                view.repetitionsDone(),
                view.intensity(),
                view.weight(),
                view.startSeries(),
                view.endSeries(),
                view.restTime(),
                view.exerciseSeriesId()
        );
    }

    /**
     * Maps a {@link SeriesSummaryView} to a {@link SeriesSummaryResponse}.
     *
     * @param view the application read model; must not be {@code null}
     * @return the lightweight REST response DTO
     */
    public SeriesSummaryResponse toSummaryResponse(SeriesSummaryView view) {
        Objects.requireNonNull(view, "SeriesSummaryView must not be null");
        return new SeriesSummaryResponse(
                view.id(),
                view.serialNumber(),
                view.repetitionsToDo(),
                view.intensity(),
                view.weight(),
                view.restTime()
        );
    }

    /**
     * Maps a list of {@link SeriesSummaryView} to a list of {@link SeriesSummaryResponse}.
     *
     * @param views the application read models; must not be {@code null}
     * @return list of lightweight REST response DTOs
     */
    public List<SeriesSummaryResponse> toSummaryResponseList(List<SeriesSummaryView> views) {
        Objects.requireNonNull(views, "SeriesSummaryView list must not be null");
        return views.stream().map(this::toSummaryResponse).toList();
    }
}


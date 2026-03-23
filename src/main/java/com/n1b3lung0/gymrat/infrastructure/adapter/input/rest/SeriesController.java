package com.n1b3lung0.gymrat.infrastructure.adapter.input.rest;

import com.n1b3lung0.gymrat.application.dto.DeleteSeriesCommand;
import com.n1b3lung0.gymrat.application.dto.GetSeriesByIdQuery;
import com.n1b3lung0.gymrat.application.dto.ListSeriesByExerciseSeriesQuery;
import com.n1b3lung0.gymrat.application.port.input.command.DeleteSeriesUseCase;
import com.n1b3lung0.gymrat.application.port.input.command.RecordSeriesUseCase;
import com.n1b3lung0.gymrat.application.port.input.command.UpdateSeriesUseCase;
import com.n1b3lung0.gymrat.application.port.input.query.GetSeriesByIdUseCase;
import com.n1b3lung0.gymrat.application.port.input.query.ListSeriesByExerciseSeriesUseCase;
import com.n1b3lung0.gymrat.domain.model.ExerciseSeriesId;
import com.n1b3lung0.gymrat.domain.model.SeriesId;
import com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto.RecordSeriesRequest;
import com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto.SeriesResponse;
import com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto.SeriesSummaryResponse;
import com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto.UpdateSeriesRequest;
import com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.mapper.SeriesRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for Series — individual sets within an exercise session.
 *
 * <p>All endpoints are nested under
 * {@code /api/v1/workouts/{workoutId}/exercises/{exerciseSeriesId}/series}
 * to reflect the full parent-child chain.
 *
 * <ul>
 *   <li>{@code POST}   → {@code 201 Created} + {@code Location} header</li>
 *   <li>{@code GET}    → {@code 200 OK}</li>
 *   <li>{@code PUT}    → {@code 200 OK} (full replacement)</li>
 *   <li>{@code DELETE} → {@code 204 No Content}</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/workouts/{workoutId}/exercises/{exerciseSeriesId}/series")
@Tag(name = "Series", description = "Manage individual sets within an exercise session")
public class SeriesController {

    private final RecordSeriesUseCase              recordSeriesUseCase;
    private final UpdateSeriesUseCase              updateSeriesUseCase;
    private final DeleteSeriesUseCase              deleteSeriesUseCase;
    private final GetSeriesByIdUseCase             getSeriesByIdUseCase;
    private final ListSeriesByExerciseSeriesUseCase listSeriesUseCase;
    private final SeriesRestMapper                 mapper;

    public SeriesController(
            RecordSeriesUseCase recordSeriesUseCase,
            UpdateSeriesUseCase updateSeriesUseCase,
            DeleteSeriesUseCase deleteSeriesUseCase,
            GetSeriesByIdUseCase getSeriesByIdUseCase,
            ListSeriesByExerciseSeriesUseCase listSeriesUseCase,
            SeriesRestMapper mapper) {
        this.recordSeriesUseCase = recordSeriesUseCase;
        this.updateSeriesUseCase = updateSeriesUseCase;
        this.deleteSeriesUseCase = deleteSeriesUseCase;
        this.getSeriesByIdUseCase = getSeriesByIdUseCase;
        this.listSeriesUseCase   = listSeriesUseCase;
        this.mapper              = mapper;
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/workouts/{workoutId}/exercises/{exerciseSeriesId}/series
    // -------------------------------------------------------------------------

    @PostMapping
    @Operation(
            summary = "Record a new series set",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Series created",
                            headers = @Header(name = "Location",
                                    description = "URL of the newly created series",
                                    schema = @Schema(type = "string"))),
                    @ApiResponse(responseCode = "404", description = "ExerciseSeries not found",
                            content = @Content(mediaType = "application/problem+json")),
                    @ApiResponse(responseCode = "422", description = "Validation failed",
                            content = @Content(mediaType = "application/problem+json"))
            }
    )
    public ResponseEntity<Void> record(
            @Parameter(description = "Workout UUID") @PathVariable UUID workoutId,
            @Parameter(description = "ExerciseSeries UUID") @PathVariable UUID exerciseSeriesId,
            @Valid @RequestBody RecordSeriesRequest request) {

        var id = recordSeriesUseCase.execute(mapper.toRecordCommand(exerciseSeriesId, request));

        var location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id.value())
                .toUri();

        return ResponseEntity.created(location).build();
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/workouts/{workoutId}/exercises/{exerciseSeriesId}/series
    // -------------------------------------------------------------------------

    @GetMapping
    @Operation(
            summary = "List all series in an exercise session (ordered by serialNumber)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of series")
            }
    )
    public ResponseEntity<List<SeriesSummaryResponse>> list(
            @Parameter(description = "Workout UUID") @PathVariable UUID workoutId,
            @Parameter(description = "ExerciseSeries UUID") @PathVariable UUID exerciseSeriesId) {

        var views = listSeriesUseCase.execute(
                new ListSeriesByExerciseSeriesQuery(ExerciseSeriesId.of(exerciseSeriesId)));
        return ResponseEntity.ok(mapper.toSummaryResponseList(views));
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/workouts/{workoutId}/exercises/{exerciseSeriesId}/series/{seriesId}
    // -------------------------------------------------------------------------

    @GetMapping("/{seriesId}")
    @Operation(
            summary = "Get a series set by ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Series found"),
                    @ApiResponse(responseCode = "404", description = "Series not found",
                            content = @Content(mediaType = "application/problem+json"))
            }
    )
    public ResponseEntity<SeriesResponse> getById(
            @Parameter(description = "Workout UUID") @PathVariable UUID workoutId,
            @Parameter(description = "ExerciseSeries UUID") @PathVariable UUID exerciseSeriesId,
            @Parameter(description = "Series UUID") @PathVariable UUID seriesId) {

        var view = getSeriesByIdUseCase.execute(
                new GetSeriesByIdQuery(SeriesId.of(seriesId)));
        return ResponseEntity.ok(mapper.toResponse(view));
    }

    // -------------------------------------------------------------------------
    // PUT /api/v1/workouts/{workoutId}/exercises/{exerciseSeriesId}/series/{seriesId}
    // -------------------------------------------------------------------------

    @PutMapping("/{seriesId}")
    @Operation(
            summary = "Update a series set (full replacement)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Series updated"),
                    @ApiResponse(responseCode = "404", description = "Series not found",
                            content = @Content(mediaType = "application/problem+json")),
                    @ApiResponse(responseCode = "422", description = "Validation failed",
                            content = @Content(mediaType = "application/problem+json"))
            }
    )
    public ResponseEntity<SeriesResponse> update(
            @Parameter(description = "Workout UUID") @PathVariable UUID workoutId,
            @Parameter(description = "ExerciseSeries UUID") @PathVariable UUID exerciseSeriesId,
            @Parameter(description = "Series UUID") @PathVariable UUID seriesId,
            @Valid @RequestBody UpdateSeriesRequest request) {

        updateSeriesUseCase.execute(mapper.toUpdateCommand(seriesId, request));

        var view = getSeriesByIdUseCase.execute(new GetSeriesByIdQuery(SeriesId.of(seriesId)));
        return ResponseEntity.ok(mapper.toResponse(view));
    }

    // -------------------------------------------------------------------------
    // DELETE /api/v1/workouts/{workoutId}/exercises/{exerciseSeriesId}/series/{seriesId}
    // -------------------------------------------------------------------------

    @DeleteMapping("/{seriesId}")
    @Operation(
            summary = "Soft-delete a series set",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Series deleted"),
                    @ApiResponse(responseCode = "404", description = "Series not found",
                            content = @Content(mediaType = "application/problem+json"))
            }
    )
    public ResponseEntity<Void> delete(
            @Parameter(description = "Workout UUID") @PathVariable UUID workoutId,
            @Parameter(description = "ExerciseSeries UUID") @PathVariable UUID exerciseSeriesId,
            @Parameter(description = "Series UUID") @PathVariable UUID seriesId) {

        deleteSeriesUseCase.execute(new DeleteSeriesCommand(SeriesId.of(seriesId)));
        return ResponseEntity.noContent().build();
    }
}


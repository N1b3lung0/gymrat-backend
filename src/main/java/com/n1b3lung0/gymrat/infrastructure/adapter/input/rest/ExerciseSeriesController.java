package com.n1b3lung0.gymrat.infrastructure.adapter.input.rest;

import com.n1b3lung0.gymrat.application.dto.GetExerciseSeriesByIdQuery;
import com.n1b3lung0.gymrat.application.dto.ListExerciseSeriesByWorkoutQuery;
import com.n1b3lung0.gymrat.application.dto.RemoveExerciseFromWorkoutCommand;
import com.n1b3lung0.gymrat.application.port.input.command.AddExerciseToWorkoutUseCase;
import com.n1b3lung0.gymrat.application.port.input.command.RemoveExerciseFromWorkoutUseCase;
import com.n1b3lung0.gymrat.application.port.input.query.GetExerciseSeriesByIdUseCase;
import com.n1b3lung0.gymrat.application.port.input.query.ListExerciseSeriesByWorkoutUseCase;
import com.n1b3lung0.gymrat.domain.model.ExerciseSeriesId;
import com.n1b3lung0.gymrat.domain.model.WorkoutId;
import com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto.AddExerciseToWorkoutRequest;
import com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto.ExerciseSeriesResponse;
import com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto.ExerciseSeriesSummaryResponse;
import com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.mapper.ExerciseSeriesRestMapper;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for ExerciseSeries — exercise sessions within a workout.
 *
 * <p>All endpoints are nested under {@code /api/v1/workouts/{workoutId}/exercises}
 * to reflect the parent-child relationship.
 *
 * <ul>
 *   <li>{@code POST}   → {@code 201 Created} + {@code Location} header</li>
 *   <li>{@code GET}    → {@code 200 OK}</li>
 *   <li>{@code DELETE} → {@code 204 No Content}</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/workouts/{workoutId}/exercises")
@Tag(name = "Exercise Series", description = "Manage exercises within a workout session")
public class ExerciseSeriesController {

    private final AddExerciseToWorkoutUseCase      addExerciseToWorkoutUseCase;
    private final RemoveExerciseFromWorkoutUseCase removeExerciseFromWorkoutUseCase;
    private final GetExerciseSeriesByIdUseCase     getExerciseSeriesByIdUseCase;
    private final ListExerciseSeriesByWorkoutUseCase listExerciseSeriesByWorkoutUseCase;
    private final ExerciseSeriesRestMapper          mapper;

    public ExerciseSeriesController(
            AddExerciseToWorkoutUseCase addExerciseToWorkoutUseCase,
            RemoveExerciseFromWorkoutUseCase removeExerciseFromWorkoutUseCase,
            GetExerciseSeriesByIdUseCase getExerciseSeriesByIdUseCase,
            ListExerciseSeriesByWorkoutUseCase listExerciseSeriesByWorkoutUseCase,
            ExerciseSeriesRestMapper mapper) {
        this.addExerciseToWorkoutUseCase       = addExerciseToWorkoutUseCase;
        this.removeExerciseFromWorkoutUseCase  = removeExerciseFromWorkoutUseCase;
        this.getExerciseSeriesByIdUseCase      = getExerciseSeriesByIdUseCase;
        this.listExerciseSeriesByWorkoutUseCase = listExerciseSeriesByWorkoutUseCase;
        this.mapper                            = mapper;
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/workouts/{workoutId}/exercises
    // -------------------------------------------------------------------------

    @PostMapping
    @Operation(
            summary = "Add an exercise to a workout",
            responses = {
                    @ApiResponse(responseCode = "201", description = "ExerciseSeries created",
                            headers = @Header(name = "Location",
                                    description = "URL of the newly created exercise-series",
                                    schema = @Schema(type = "string"))),
                    @ApiResponse(responseCode = "404", description = "Workout or Exercise not found",
                            content = @Content(mediaType = "application/problem+json")),
                    @ApiResponse(responseCode = "422", description = "Validation failed",
                            content = @Content(mediaType = "application/problem+json"))
            }
    )
    public ResponseEntity<Void> add(
            @Parameter(description = "Workout UUID") @PathVariable UUID workoutId,
            @Valid @RequestBody AddExerciseToWorkoutRequest request) {

        var id = addExerciseToWorkoutUseCase.execute(mapper.toAddCommand(workoutId, request));

        var location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id.value())
                .toUri();

        return ResponseEntity.created(location).build();
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/workouts/{workoutId}/exercises
    // -------------------------------------------------------------------------

    @GetMapping
    @Operation(
            summary = "List all exercises in a workout",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of exercise-series"),
                    @ApiResponse(responseCode = "404", description = "Workout not found",
                            content = @Content(mediaType = "application/problem+json"))
            }
    )
    public ResponseEntity<List<ExerciseSeriesSummaryResponse>> list(
            @Parameter(description = "Workout UUID") @PathVariable UUID workoutId) {

        var views = listExerciseSeriesByWorkoutUseCase.execute(
                new ListExerciseSeriesByWorkoutQuery(WorkoutId.of(workoutId)));
        return ResponseEntity.ok(mapper.toSummaryResponseList(views));
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/workouts/{workoutId}/exercises/{exerciseSeriesId}
    // -------------------------------------------------------------------------

    @GetMapping("/{exerciseSeriesId}")
    @Operation(
            summary = "Get an exercise-series by ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "ExerciseSeries found"),
                    @ApiResponse(responseCode = "404", description = "ExerciseSeries not found",
                            content = @Content(mediaType = "application/problem+json"))
            }
    )
    public ResponseEntity<ExerciseSeriesResponse> getById(
            @Parameter(description = "Workout UUID") @PathVariable UUID workoutId,
            @Parameter(description = "ExerciseSeries UUID") @PathVariable UUID exerciseSeriesId) {

        var view = getExerciseSeriesByIdUseCase.execute(
                new GetExerciseSeriesByIdQuery(ExerciseSeriesId.of(exerciseSeriesId)));
        return ResponseEntity.ok(mapper.toResponse(view));
    }

    // -------------------------------------------------------------------------
    // DELETE /api/v1/workouts/{workoutId}/exercises/{exerciseSeriesId}
    // -------------------------------------------------------------------------

    @DeleteMapping("/{exerciseSeriesId}")
    @Operation(
            summary = "Remove an exercise from a workout (soft-delete)",
            responses = {
                    @ApiResponse(responseCode = "204", description = "ExerciseSeries removed"),
                    @ApiResponse(responseCode = "404", description = "ExerciseSeries not found",
                            content = @Content(mediaType = "application/problem+json"))
            }
    )
    public ResponseEntity<Void> remove(
            @Parameter(description = "Workout UUID") @PathVariable UUID workoutId,
            @Parameter(description = "ExerciseSeries UUID") @PathVariable UUID exerciseSeriesId) {

        removeExerciseFromWorkoutUseCase.execute(
                new RemoveExerciseFromWorkoutCommand(ExerciseSeriesId.of(exerciseSeriesId)));
        return ResponseEntity.noContent().build();
    }
}


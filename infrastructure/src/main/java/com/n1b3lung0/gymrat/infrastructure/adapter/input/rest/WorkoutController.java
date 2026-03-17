package com.n1b3lung0.gymrat.infrastructure.adapter.input.rest;

import com.n1b3lung0.gymrat.application.dto.DeleteWorkoutCommand;
import com.n1b3lung0.gymrat.application.dto.GetWorkoutByIdQuery;
import com.n1b3lung0.gymrat.application.dto.ListWorkoutsQuery;
import com.n1b3lung0.gymrat.application.port.input.command.CreateWorkoutUseCase;
import com.n1b3lung0.gymrat.application.port.input.command.DeleteWorkoutUseCase;
import com.n1b3lung0.gymrat.application.port.input.command.FinishWorkoutUseCase;
import com.n1b3lung0.gymrat.application.port.input.query.GetWorkoutByIdUseCase;
import com.n1b3lung0.gymrat.application.port.input.query.ListWorkoutsUseCase;
import com.n1b3lung0.gymrat.domain.model.WorkoutId;
import com.n1b3lung0.gymrat.domain.repository.PageRequest;
import com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto.CreateWorkoutRequest;
import com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto.FinishWorkoutRequest;
import com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto.PageResponse;
import com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto.WorkoutResponse;
import com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto.WorkoutSummaryResponse;
import com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.mapper.WorkoutRestMapper;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.UUID;

/**
 * REST controller for Workout CRUD operations.
 *
 * <p>Follows the conventions defined in CLAUDE.md:
 * <ul>
 *   <li>{@code POST} → {@code 201 Created} + {@code Location} header</li>
 *   <li>{@code GET}  → {@code 200 OK}</li>
 *   <li>{@code PATCH /finish} → {@code 200 OK} (partial update)</li>
 *   <li>{@code DELETE} → {@code 204 No Content}</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/workouts")
@Tag(name = "Workouts", description = "CRUD operations for workout sessions")
public class WorkoutController {

    private final CreateWorkoutUseCase  createWorkoutUseCase;
    private final FinishWorkoutUseCase  finishWorkoutUseCase;
    private final DeleteWorkoutUseCase  deleteWorkoutUseCase;
    private final GetWorkoutByIdUseCase getWorkoutByIdUseCase;
    private final ListWorkoutsUseCase   listWorkoutsUseCase;
    private final WorkoutRestMapper     mapper;

    public WorkoutController(
            CreateWorkoutUseCase createWorkoutUseCase,
            FinishWorkoutUseCase finishWorkoutUseCase,
            DeleteWorkoutUseCase deleteWorkoutUseCase,
            GetWorkoutByIdUseCase getWorkoutByIdUseCase,
            ListWorkoutsUseCase listWorkoutsUseCase,
            WorkoutRestMapper mapper) {
        this.createWorkoutUseCase  = createWorkoutUseCase;
        this.finishWorkoutUseCase  = finishWorkoutUseCase;
        this.deleteWorkoutUseCase  = deleteWorkoutUseCase;
        this.getWorkoutByIdUseCase = getWorkoutByIdUseCase;
        this.listWorkoutsUseCase   = listWorkoutsUseCase;
        this.mapper                = mapper;
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/workouts
    // -------------------------------------------------------------------------

    @PostMapping
    @Operation(
            summary = "Start a new workout session",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Workout created",
                            headers = @Header(name = "Location",
                                    description = "URL of the newly created workout",
                                    schema = @Schema(type = "string"))),
                    @ApiResponse(responseCode = "422", description = "Validation failed",
                            content = @Content(mediaType = "application/problem+json"))
            }
    )
    public ResponseEntity<Void> create(@Valid @RequestBody CreateWorkoutRequest request) {
        var id = createWorkoutUseCase.execute(mapper.toCreateCommand(request));

        var location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id.value())
                .toUri();

        return ResponseEntity.created(location).build();
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/workouts/{id}
    // -------------------------------------------------------------------------

    @GetMapping("/{id}")
    @Operation(
            summary = "Get workout by ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Workout found"),
                    @ApiResponse(responseCode = "404", description = "Workout not found",
                            content = @Content(mediaType = "application/problem+json"))
            }
    )
    public ResponseEntity<WorkoutResponse> getById(
            @Parameter(description = "Workout UUID") @PathVariable UUID id) {

        var view = getWorkoutByIdUseCase.execute(new GetWorkoutByIdQuery(WorkoutId.of(id)));
        return ResponseEntity.ok(mapper.toResponse(view));
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/workouts
    // -------------------------------------------------------------------------

    @GetMapping
    @Operation(
            summary = "List workouts (paginated)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Paginated list of workouts")
            }
    )
    public ResponseEntity<PageResponse<WorkoutSummaryResponse>> list(
            @Parameter(description = "Zero-based page index")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Field to sort by")
            @RequestParam(required = false) String sortBy,
            @Parameter(description = "Sort ascending")
            @RequestParam(defaultValue = "true") boolean ascending) {

        var result = listWorkoutsUseCase.execute(
                new ListWorkoutsQuery(PageRequest.of(page, size, sortBy, ascending)));
        return ResponseEntity.ok(mapper.toPageResponse(result));
    }

    // -------------------------------------------------------------------------
    // PATCH /api/v1/workouts/{id}/finish
    // -------------------------------------------------------------------------

    @PatchMapping("/{id}/finish")
    @Operation(
            summary = "Finish an open workout session",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Workout finished"),
                    @ApiResponse(responseCode = "404", description = "Workout not found",
                            content = @Content(mediaType = "application/problem+json")),
                    @ApiResponse(responseCode = "422", description = "Workout already finished or validation failed",
                            content = @Content(mediaType = "application/problem+json"))
            }
    )
    public ResponseEntity<WorkoutResponse> finish(
            @Parameter(description = "Workout UUID") @PathVariable UUID id,
            @Valid @RequestBody FinishWorkoutRequest request) {

        finishWorkoutUseCase.execute(mapper.toFinishCommand(id, request));

        var view = getWorkoutByIdUseCase.execute(new GetWorkoutByIdQuery(WorkoutId.of(id)));
        return ResponseEntity.ok(mapper.toResponse(view));
    }

    // -------------------------------------------------------------------------
    // DELETE /api/v1/workouts/{id}
    // -------------------------------------------------------------------------

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Soft-delete a workout",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Workout deleted"),
                    @ApiResponse(responseCode = "404", description = "Workout not found",
                            content = @Content(mediaType = "application/problem+json"))
            }
    )
    public ResponseEntity<Void> delete(
            @Parameter(description = "Workout UUID") @PathVariable UUID id) {

        deleteWorkoutUseCase.execute(new DeleteWorkoutCommand(WorkoutId.of(id)));
        return ResponseEntity.noContent().build();
    }
}


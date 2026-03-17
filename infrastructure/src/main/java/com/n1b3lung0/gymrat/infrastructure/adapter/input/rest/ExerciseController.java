package com.n1b3lung0.gymrat.infrastructure.adapter.input.rest;

import com.n1b3lung0.gymrat.application.dto.DeleteExerciseCommand;
import com.n1b3lung0.gymrat.application.dto.GetExerciseByIdQuery;
import com.n1b3lung0.gymrat.application.dto.ListExercisesQuery;
import com.n1b3lung0.gymrat.application.port.input.command.CreateExerciseUseCase;
import com.n1b3lung0.gymrat.application.port.input.command.DeleteExerciseUseCase;
import com.n1b3lung0.gymrat.application.port.input.command.UpdateExerciseUseCase;
import com.n1b3lung0.gymrat.application.port.input.query.GetExerciseByIdUseCase;
import com.n1b3lung0.gymrat.application.port.input.query.ListExercisesUseCase;
import com.n1b3lung0.gymrat.domain.model.ExerciseId;
import com.n1b3lung0.gymrat.domain.repository.PageRequest;
import com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto.CreateExerciseRequest;
import com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto.ExerciseResponse;
import com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto.ExerciseSummaryResponse;
import com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto.PageResponse;
import com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto.UpdateExerciseRequest;
import com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.mapper.ExerciseRestMapper;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.UUID;

/**
 * REST controller for Exercise CRUD operations.
 *
 * <p>Follows the conventions defined in CLAUDE.md:
 * <ul>
 *   <li>{@code POST} → {@code 201 Created} + {@code Location} header</li>
 *   <li>{@code GET} → {@code 200 OK}</li>
 *   <li>{@code PUT} → {@code 200 OK} (full replacement)</li>
 *   <li>{@code DELETE} → {@code 204 No Content}</li>
 * </ul>
 *
 * <p>All errors return {@code application/problem+json} (RFC 9457) via
 * {@code GlobalExceptionHandler}.
 */
@RestController
@RequestMapping("/api/v1/exercises")
@Tag(name = "Exercises", description = "CRUD operations for exercises")
public class ExerciseController {

    private final CreateExerciseUseCase createExerciseUseCase;
    private final UpdateExerciseUseCase updateExerciseUseCase;
    private final DeleteExerciseUseCase deleteExerciseUseCase;
    private final GetExerciseByIdUseCase getExerciseByIdUseCase;
    private final ListExercisesUseCase  listExercisesUseCase;
    private final ExerciseRestMapper    mapper;

    public ExerciseController(
            CreateExerciseUseCase createExerciseUseCase,
            UpdateExerciseUseCase updateExerciseUseCase,
            DeleteExerciseUseCase deleteExerciseUseCase,
            GetExerciseByIdUseCase getExerciseByIdUseCase,
            ListExercisesUseCase listExercisesUseCase,
            ExerciseRestMapper mapper) {
        this.createExerciseUseCase = createExerciseUseCase;
        this.updateExerciseUseCase = updateExerciseUseCase;
        this.deleteExerciseUseCase = deleteExerciseUseCase;
        this.getExerciseByIdUseCase = getExerciseByIdUseCase;
        this.listExercisesUseCase   = listExercisesUseCase;
        this.mapper = mapper;
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/exercises
    // -------------------------------------------------------------------------

    @PostMapping
    @Operation(
            summary = "Create a new exercise",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Exercise created",
                            headers = @Header(name = "Location",
                                    description = "URL of the newly created exercise",
                                    schema = @Schema(type = "string"))),
                    @ApiResponse(responseCode = "400", description = "Invalid request body",
                            content = @Content(mediaType = "application/problem+json")),
                    @ApiResponse(responseCode = "409", description = "Exercise name already exists",
                            content = @Content(mediaType = "application/problem+json")),
                    @ApiResponse(responseCode = "422", description = "Validation failed",
                            content = @Content(mediaType = "application/problem+json"))
            }
    )
    public ResponseEntity<Void> create(
            @Valid @RequestBody CreateExerciseRequest request,
            @Parameter(description = "Client-supplied UUID for idempotent creation")
            @RequestHeader(value = "Idempotency-Key", required = false) UUID idempotencyKey) {

        ExerciseId id = createExerciseUseCase.execute(mapper.toCreateCommand(request, idempotencyKey));

        var location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id.value())
                .toUri();

        return ResponseEntity.created(location).build();
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/exercises/{id}
    // -------------------------------------------------------------------------

    @GetMapping("/{id}")
    @Operation(
            summary = "Get exercise by ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Exercise found"),
                    @ApiResponse(responseCode = "404", description = "Exercise not found",
                            content = @Content(mediaType = "application/problem+json"))
            }
    )
    public ResponseEntity<ExerciseResponse> getById(
            @Parameter(description = "Exercise UUID") @PathVariable UUID id) {

        var view = getExerciseByIdUseCase.execute(new GetExerciseByIdQuery(ExerciseId.of(id)));
        return ResponseEntity.ok(mapper.toResponse(view));
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/exercises
    // -------------------------------------------------------------------------

    @GetMapping
    @Operation(
            summary = "List exercises (paginated)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Paginated list of exercises")
            }
    )
    public ResponseEntity<PageResponse<ExerciseSummaryResponse>> list(
            @Parameter(description = "Zero-based page index")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Field to sort by")
            @RequestParam(required = false) String sortBy,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "true") boolean ascending) {

        var result = listExercisesUseCase.execute(
                new ListExercisesQuery(PageRequest.of(page, size, sortBy, ascending)));
        return ResponseEntity.ok(mapper.toPageResponse(result));
    }

    // -------------------------------------------------------------------------
    // PUT /api/v1/exercises/{id}
    // -------------------------------------------------------------------------

    @PutMapping("/{id}")
    @Operation(
            summary = "Update an exercise (full replacement)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Exercise updated"),
                    @ApiResponse(responseCode = "404", description = "Exercise not found",
                            content = @Content(mediaType = "application/problem+json")),
                    @ApiResponse(responseCode = "409", description = "Exercise name already exists",
                            content = @Content(mediaType = "application/problem+json")),
                    @ApiResponse(responseCode = "422", description = "Validation failed",
                            content = @Content(mediaType = "application/problem+json"))
            }
    )
    public ResponseEntity<ExerciseResponse> update(
            @Parameter(description = "Exercise UUID") @PathVariable UUID id,
            @Valid @RequestBody UpdateExerciseRequest request,
            @Parameter(description = "Client-supplied UUID for idempotent update")
            @RequestHeader(value = "Idempotency-Key", required = false) UUID idempotencyKey) {

        updateExerciseUseCase.execute(mapper.toUpdateCommand(id, request));

        // Re-fetch to return updated state
        var view = getExerciseByIdUseCase.execute(new GetExerciseByIdQuery(ExerciseId.of(id)));
        return ResponseEntity.ok(mapper.toResponse(view));
    }

    // -------------------------------------------------------------------------
    // DELETE /api/v1/exercises/{id}
    // -------------------------------------------------------------------------

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Soft-delete an exercise",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Exercise deleted"),
                    @ApiResponse(responseCode = "404", description = "Exercise not found",
                            content = @Content(mediaType = "application/problem+json"))
            }
    )
    public ResponseEntity<Void> delete(
            @Parameter(description = "Exercise UUID") @PathVariable UUID id) {

        deleteExerciseUseCase.execute(new DeleteExerciseCommand(ExerciseId.of(id)));
        return ResponseEntity.noContent().build();
    }
}


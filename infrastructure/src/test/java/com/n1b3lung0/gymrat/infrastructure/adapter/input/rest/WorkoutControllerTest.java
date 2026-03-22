package com.n1b3lung0.gymrat.infrastructure.adapter.input.rest;

import com.n1b3lung0.gymrat.application.dto.WorkoutDetailView;
import com.n1b3lung0.gymrat.application.dto.WorkoutSummaryView;
import com.n1b3lung0.gymrat.application.port.input.command.CreateWorkoutUseCase;
import com.n1b3lung0.gymrat.application.port.input.command.DeleteWorkoutUseCase;
import com.n1b3lung0.gymrat.application.port.input.command.FinishWorkoutUseCase;
import com.n1b3lung0.gymrat.application.port.input.query.GetWorkoutByIdUseCase;
import com.n1b3lung0.gymrat.application.port.input.query.ListWorkoutsUseCase;
import com.n1b3lung0.gymrat.domain.exception.WorkoutAlreadyFinishedException;
import com.n1b3lung0.gymrat.domain.exception.WorkoutNotFoundException;
import com.n1b3lung0.gymrat.domain.model.WorkoutId;
import com.n1b3lung0.gymrat.domain.repository.PageResult;
import com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto.PageResponse;
import com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto.WorkoutResponse;
import com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto.WorkoutSummaryResponse;
import com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.mapper.WorkoutRestMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller slice test for {@link WorkoutController}.
 *
 * <p>Uses {@code MockMvcBuilders.standaloneSetup()} — same pattern as {@code ExerciseControllerTest}.
 * {@link GlobalExceptionHandler} is registered to validate error response codes.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WorkoutController")
class WorkoutControllerTest {

    private static final String BASE_URL = "/api/v1/workouts";

    @Mock CreateWorkoutUseCase  createWorkoutUseCase;
    @Mock FinishWorkoutUseCase  finishWorkoutUseCase;
    @Mock DeleteWorkoutUseCase  deleteWorkoutUseCase;
    @Mock GetWorkoutByIdUseCase getWorkoutByIdUseCase;
    @Mock ListWorkoutsUseCase   listWorkoutsUseCase;
    @Mock WorkoutRestMapper     mapper;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        var controller = new WorkoutController(
                createWorkoutUseCase, finishWorkoutUseCase, deleteWorkoutUseCase,
                getWorkoutByIdUseCase, listWorkoutsUseCase, mapper);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new JacksonJsonHttpMessageConverter())
                .build();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static WorkoutId randomId() {
        return WorkoutId.generate();
    }

    private static WorkoutResponse workoutResponse(UUID id) {
        return new WorkoutResponse(id, Instant.parse("2026-03-22T10:00:00Z"), null, false, List.of());
    }

    private static WorkoutSummaryResponse summaryResponse(UUID id) {
        return new WorkoutSummaryResponse(id, Instant.parse("2026-03-22T10:00:00Z"), null, false);
    }

    private static WorkoutDetailView detailView(UUID id) {
        return new WorkoutDetailView(id, Instant.parse("2026-03-22T10:00:00Z"), null, List.of());
    }

    private static String createRequestJson() {
        return """
                {
                  "startWorkout": "2026-03-22T10:00:00Z"
                }
                """;
    }

    private static String finishRequestJson() {
        return """
                {
                  "endWorkout": "2026-03-22T11:00:00Z"
                }
                """;
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/workouts
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("POST /api/v1/workouts")
    class Create {

        @Test
        @DisplayName("should return 201 Created with Location header when workout is created")
        void shouldReturn201_whenCreateWorkout() throws Exception {
            var id = randomId();
            when(mapper.toCreateCommand(any())).thenReturn(null);
            when(createWorkoutUseCase.execute(any())).thenReturn(id);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createRequestJson()))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location",
                            containsString("/api/v1/workouts/" + id.value())));
        }

        @Test
        @DisplayName("should return 422 when startWorkout is missing")
        void shouldReturn422_whenStartWorkoutIsMissing() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isUnprocessableEntity());

            verify(createWorkoutUseCase, never()).execute(any());
        }
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/workouts/{id}
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GET /api/v1/workouts/{id}")
    class GetById {

        @Test
        @DisplayName("should return 200 with workout detail when found")
        void shouldReturn200_whenWorkoutFound() throws Exception {
            var id   = randomId();
            var view = detailView(id.value());
            var resp = workoutResponse(id.value());
            when(getWorkoutByIdUseCase.execute(any())).thenReturn(view);
            when(mapper.toResponse(view)).thenReturn(resp);

            mockMvc.perform(get(BASE_URL + "/" + id.value()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(id.value().toString())))
                    .andExpect(jsonPath("$.finished", is(false)));
        }

        @Test
        @DisplayName("should return 404 when workout not found")
        void shouldReturn404_whenWorkoutNotFound() throws Exception {
            var id = randomId();
            when(getWorkoutByIdUseCase.execute(any()))
                    .thenThrow(new WorkoutNotFoundException(id));

            mockMvc.perform(get(BASE_URL + "/" + id.value()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should return 400 when id is not a valid UUID")
        void shouldReturn400_whenIdIsInvalidUuid() throws Exception {
            mockMvc.perform(get(BASE_URL + "/not-a-uuid"))
                    .andExpect(status().isBadRequest());
        }
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/workouts
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GET /api/v1/workouts")
    class ListWorkouts {

        @Test
        @DisplayName("should return 200 with paged response when workouts exist")
        void shouldReturn200WithPage_whenListWorkouts() throws Exception {
            var id1     = UUID.randomUUID();
            var id2     = UUID.randomUUID();
            var page    = new PageResult<>(
                    List.of(
                            new WorkoutSummaryView(id1, Instant.parse("2026-03-22T09:00:00Z"), null, false),
                            new WorkoutSummaryView(id2, Instant.parse("2026-03-21T09:00:00Z"), Instant.parse("2026-03-21T10:00:00Z"), true)
                    ), 0, 20, 2L);
            var pageResp = new PageResponse<>(
                    List.of(summaryResponse(id1), summaryResponse(id2)), 0, 20, 2L, 1, true);

            when(listWorkoutsUseCase.execute(any())).thenReturn(page);
            when(mapper.toPageResponse(page)).thenReturn(pageResp);

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.totalElements", is(2)))
                    .andExpect(jsonPath("$.page", is(0)))
                    .andExpect(jsonPath("$.last", is(true)));
        }

        @Test
        @DisplayName("should return 200 with empty page when no workouts exist")
        void shouldReturn200WithEmptyPage_whenNoWorkouts() throws Exception {
            var emptyPage = new PageResult<WorkoutSummaryView>(List.of(), 0, 20, 0L);
            var emptyResp = new PageResponse<WorkoutSummaryResponse>(List.of(), 0, 20, 0L, 0, true);

            when(listWorkoutsUseCase.execute(any())).thenReturn(emptyPage);
            when(mapper.toPageResponse(emptyPage)).thenReturn(emptyResp);

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)))
                    .andExpect(jsonPath("$.totalElements", is(0)));
        }
    }

    // -------------------------------------------------------------------------
    // PATCH /api/v1/workouts/{id}/finish
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("PATCH /api/v1/workouts/{id}/finish")
    class Finish {

        @Test
        @DisplayName("should return 200 with finished workout on success")
        void shouldReturn200_whenWorkoutFinished() throws Exception {
            var id   = randomId();
            var view = new WorkoutDetailView(id.value(),
                    Instant.parse("2026-03-22T10:00:00Z"),
                    Instant.parse("2026-03-22T11:00:00Z"),
                    List.of());
            var resp = new WorkoutResponse(id.value(),
                    Instant.parse("2026-03-22T10:00:00Z"),
                    Instant.parse("2026-03-22T11:00:00Z"),
                    true, List.of());

            when(mapper.toFinishCommand(any(), any())).thenReturn(null);
            when(getWorkoutByIdUseCase.execute(any())).thenReturn(view);
            when(mapper.toResponse(view)).thenReturn(resp);

            mockMvc.perform(patch(BASE_URL + "/" + id.value() + "/finish")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(finishRequestJson()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.finished", is(true)));
        }

        @Test
        @DisplayName("should return 404 when workout not found on finish")
        void shouldReturn404_whenWorkoutNotFound() throws Exception {
            var id = randomId();
            when(mapper.toFinishCommand(any(), any())).thenReturn(null);
            doThrow(new WorkoutNotFoundException(id))
                    .when(finishWorkoutUseCase).execute(any());

            mockMvc.perform(patch(BASE_URL + "/" + id.value() + "/finish")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(finishRequestJson()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should return 422 when workout is already finished")
        void shouldReturn422_whenAlreadyFinished() throws Exception {
            var id = randomId();
            when(mapper.toFinishCommand(any(), any())).thenReturn(null);
            doThrow(new WorkoutAlreadyFinishedException(id))
                    .when(finishWorkoutUseCase).execute(any());

            mockMvc.perform(patch(BASE_URL + "/" + id.value() + "/finish")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(finishRequestJson()))
                    .andExpect(status().isUnprocessableEntity());
        }
    }

    // -------------------------------------------------------------------------
    // DELETE /api/v1/workouts/{id}
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("DELETE /api/v1/workouts/{id}")
    class Delete {

        @Test
        @DisplayName("should return 204 No Content when workout is deleted")
        void shouldReturn204_whenDeleteWorkout() throws Exception {
            var id = randomId();

            mockMvc.perform(delete(BASE_URL + "/" + id.value()))
                    .andExpect(status().isNoContent());

            verify(deleteWorkoutUseCase).execute(any());
        }

        @Test
        @DisplayName("should return 404 when workout not found on delete")
        void shouldReturn404_whenWorkoutNotFound() throws Exception {
            var id = randomId();
            doThrow(new WorkoutNotFoundException(id))
                    .when(deleteWorkoutUseCase).execute(any());

            mockMvc.perform(delete(BASE_URL + "/" + id.value()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should return 400 when id is not a valid UUID")
        void shouldReturn400_whenIdIsInvalidUuid() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/not-a-uuid"))
                    .andExpect(status().isBadRequest());
        }
    }
}


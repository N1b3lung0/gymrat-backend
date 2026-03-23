package com.n1b3lung0.gymrat.infrastructure.adapter.input.rest;

import com.n1b3lung0.gymrat.application.dto.ExerciseDetailView;
import com.n1b3lung0.gymrat.application.dto.ExerciseSummaryView;
import com.n1b3lung0.gymrat.application.port.input.command.CreateExerciseUseCase;
import com.n1b3lung0.gymrat.application.port.input.command.DeleteExerciseUseCase;
import com.n1b3lung0.gymrat.application.port.input.command.UpdateExerciseUseCase;
import com.n1b3lung0.gymrat.application.port.input.query.GetExerciseByIdUseCase;
import com.n1b3lung0.gymrat.application.port.input.query.ListExercisesUseCase;
import com.n1b3lung0.gymrat.domain.exception.DuplicateExerciseNameException;
import com.n1b3lung0.gymrat.domain.exception.ExerciseNotFoundException;
import com.n1b3lung0.gymrat.domain.model.ExerciseId;
import com.n1b3lung0.gymrat.domain.model.Level;
import com.n1b3lung0.gymrat.domain.model.Muscle;
import com.n1b3lung0.gymrat.domain.model.Routine;
import com.n1b3lung0.gymrat.domain.repository.PageResult;
import com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto.ExerciseResponse;
import com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto.ExerciseSummaryResponse;
import com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto.PageResponse;
import com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.mapper.ExerciseRestMapper;
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

import java.util.List;
import java.util.Set;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller slice test for {@link ExerciseController}.
 *
 * <p>Uses {@code MockMvcBuilders.standaloneSetup()} — no Spring context required.
 * All use-case and mapper dependencies are Mockito mocks.
 * The {@link GlobalExceptionHandler} is registered to validate error response codes.
 *
 * <p>Note: {@code @WebMvcTest} was removed in Spring Boot 4.x; the standalone
 * MockMvc setup is the recommended lightweight replacement.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ExerciseController")
class ExerciseControllerTest {

    private static final String BASE_URL = "/api/v1/exercises";

    @Mock CreateExerciseUseCase  createExerciseUseCase;
    @Mock UpdateExerciseUseCase  updateExerciseUseCase;
    @Mock DeleteExerciseUseCase  deleteExerciseUseCase;
    @Mock GetExerciseByIdUseCase getExerciseByIdUseCase;
    @Mock ListExercisesUseCase   listExercisesUseCase;
    @Mock ExerciseRestMapper     mapper;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        var controller = new ExerciseController(
                createExerciseUseCase, updateExerciseUseCase, deleteExerciseUseCase,
                getExerciseByIdUseCase, listExercisesUseCase, mapper);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new JacksonJsonHttpMessageConverter())
                .build();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static ExerciseId randomId() {
        return ExerciseId.generate();
    }

    private static ExerciseResponse exerciseResponse(UUID id) {
        return new ExerciseResponse(
                id,
                "Bench Press",
                "Classic chest press on a flat bench",
                Level.INTERMEDIATE,
                Set.of(Routine.PUSH),
                Muscle.CHEST,
                Set.of(Muscle.TRICEPS, Muscle.SHOULDERS),
                null,
                null
        );
    }

    private static ExerciseSummaryResponse summaryResponse(UUID id) {
        return new ExerciseSummaryResponse(id, "Bench Press", Level.INTERMEDIATE, Muscle.CHEST, Set.of(Routine.PUSH));
    }

    private static ExerciseDetailView detailView(UUID id) {
        return new ExerciseDetailView(
                id,
                "Bench Press",
                "Classic chest press on a flat bench",
                Level.INTERMEDIATE,
                Set.of(Routine.PUSH),
                Muscle.CHEST,
                Set.of(Muscle.TRICEPS, Muscle.SHOULDERS),
                null,
                null
        );
    }

    private static String createRequestJson() {
        return """
                {
                  "name": "Bench Press",
                  "description": "Classic chest press",
                  "level": "INTERMEDIATE",
                  "routines": ["PUSH"],
                  "primaryMuscle": "CHEST",
                  "secondaryMuscles": ["TRICEPS"]
                }
                """;
    }

    private static String updateRequestJson() {
        return """
                {
                  "name": "Incline Bench Press",
                  "description": "Incline chest press",
                  "level": "ADVANCED",
                  "routines": ["PUSH"],
                  "primaryMuscle": "CHEST",
                  "secondaryMuscles": ["SHOULDERS"]
                }
                """;
    }

    private static String invalidRequestJson() {
        return """
                {
                  "name": "",
                  "level": "INTERMEDIATE",
                  "routines": ["PUSH"],
                  "primaryMuscle": "CHEST"
                }
                """;
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/exercises
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("POST /api/v1/exercises")
    class Create {

        @Test
        @DisplayName("should return 201 Created with Location header when exercise is created")
        void shouldReturn201_whenCreateExercise() throws Exception {
            var id = randomId();
            when(mapper.toCreateCommand(any(), any())).thenReturn(null);
            when(createExerciseUseCase.execute(any())).thenReturn(id);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createRequestJson()))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location",
                            containsString("/api/v1/exercises/" + id.value())));
        }

        @Test
        @DisplayName("should return 422 when name is blank")
        void shouldReturn422_whenNameIsBlank() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidRequestJson()))
                    .andExpect(status().isUnprocessableEntity());

            verify(createExerciseUseCase, never()).execute(any());
        }

        @Test
        @DisplayName("should return 422 when level is missing")
        void shouldReturn422_whenLevelIsMissing() throws Exception {
            String body = """
                    {
                      "name": "Squat",
                      "routines": ["LEG"],
                      "primaryMuscle": "QUADRICEPS"
                    }
                    """;

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isUnprocessableEntity());

            verify(createExerciseUseCase, never()).execute(any());
        }

        @Test
        @DisplayName("should return 422 when routines is empty")
        void shouldReturn422_whenRoutinesIsEmpty() throws Exception {
            String body = """
                    {
                      "name": "Squat",
                      "level": "BEGINNER",
                      "routines": [],
                      "primaryMuscle": "QUADRICEPS"
                    }
                    """;

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isUnprocessableEntity());

            verify(createExerciseUseCase, never()).execute(any());
        }

        @Test
        @DisplayName("should return 409 when exercise name already exists")
        void shouldReturn409_whenNameAlreadyExists() throws Exception {
            when(mapper.toCreateCommand(any(), any())).thenReturn(null);
            when(createExerciseUseCase.execute(any()))
                    .thenThrow(new DuplicateExerciseNameException("Bench Press"));

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createRequestJson()))
                    .andExpect(status().isConflict());
        }
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/exercises/{id}
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GET /api/v1/exercises/{id}")
    class GetById {

        @Test
        @DisplayName("should return 200 with exercise detail when found")
        void shouldReturn200_whenExerciseFound() throws Exception {
            var id   = randomId();
            var view = detailView(id.value());
            var resp = exerciseResponse(id.value());
            when(getExerciseByIdUseCase.execute(any())).thenReturn(view);
            when(mapper.toResponse(view)).thenReturn(resp);

            mockMvc.perform(get(BASE_URL + "/" + id.value()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(id.value().toString())))
                    .andExpect(jsonPath("$.name", is("Bench Press")));
        }

        @Test
        @DisplayName("should return 404 when exercise not found")
        void shouldReturn404_whenExerciseNotFound() throws Exception {
            var id = randomId();
            when(getExerciseByIdUseCase.execute(any()))
                    .thenThrow(new ExerciseNotFoundException(id));

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
    // GET /api/v1/exercises
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GET /api/v1/exercises")
    class ListExercises {

        @Test
        @DisplayName("should return 200 with paged response when exercises exist")
        void shouldReturn200WithPage_whenListExercises() throws Exception {
            var id1   = UUID.randomUUID();
            var id2   = UUID.randomUUID();
            var page  = new PageResult<>(
                    List.of(
                            new ExerciseSummaryView(id1, "Bench Press", Level.INTERMEDIATE, Muscle.CHEST, Set.of(Routine.PUSH)),
                            new ExerciseSummaryView(id2, "Squat",       Level.BEGINNER,     Muscle.QUADRICEPS, Set.of(Routine.LEG))
                    ), 0, 20, 2L);
            var pageResp = new PageResponse<>(
                    List.of(summaryResponse(id1), summaryResponse(id2)), 0, 20, 2L, 1, true);

            when(listExercisesUseCase.execute(any())).thenReturn(page);
            when(mapper.toPageResponse(page)).thenReturn(pageResp);

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.totalElements", is(2)))
                    .andExpect(jsonPath("$.page", is(0)))
                    .andExpect(jsonPath("$.last", is(true)));
        }

        @Test
        @DisplayName("should return 200 with empty page when no exercises exist")
        void shouldReturn200WithEmptyPage_whenNoExercises() throws Exception {
            var emptyPage = new PageResult<ExerciseSummaryView>(List.of(), 0, 20, 0L);
            var emptyResp = new PageResponse<ExerciseSummaryResponse>(List.of(), 0, 20, 0L, 0, true);

            when(listExercisesUseCase.execute(any())).thenReturn(emptyPage);
            when(mapper.toPageResponse(emptyPage)).thenReturn(emptyResp);

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(0)))
                    .andExpect(jsonPath("$.totalElements", is(0)));
        }
    }

    // -------------------------------------------------------------------------
    // PUT /api/v1/exercises/{id}
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("PUT /api/v1/exercises/{id}")
    class Update {

        @Test
        @DisplayName("should return 200 with updated exercise on success")
        void shouldReturn200_whenExerciseUpdated() throws Exception {
            var id   = randomId();
            var view = detailView(id.value());
            var resp = exerciseResponse(id.value());
            when(mapper.toUpdateCommand(any(), any())).thenReturn(null);
            when(getExerciseByIdUseCase.execute(any())).thenReturn(view);
            when(mapper.toResponse(view)).thenReturn(resp);

            mockMvc.perform(put(BASE_URL + "/" + id.value())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateRequestJson()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(id.value().toString())));
        }

        @Test
        @DisplayName("should return 404 when exercise not found on update")
        void shouldReturn404_whenExerciseNotFound() throws Exception {
            var id = randomId();
            when(mapper.toUpdateCommand(any(), any())).thenReturn(null);
            doThrow(new ExerciseNotFoundException(id))
                    .when(updateExerciseUseCase).execute(any());

            mockMvc.perform(put(BASE_URL + "/" + id.value())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateRequestJson()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should return 409 when new name already exists on update")
        void shouldReturn409_whenNameAlreadyExists() throws Exception {
            var id = randomId();
            when(mapper.toUpdateCommand(any(), any())).thenReturn(null);
            doThrow(new DuplicateExerciseNameException("Incline Bench Press"))
                    .when(updateExerciseUseCase).execute(any());

            mockMvc.perform(put(BASE_URL + "/" + id.value())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateRequestJson()))
                    .andExpect(status().isConflict());
        }
    }

    // -------------------------------------------------------------------------
    // DELETE /api/v1/exercises/{id}
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("DELETE /api/v1/exercises/{id}")
    class Delete {

        @Test
        @DisplayName("should return 204 No Content when exercise is deleted")
        void shouldReturn204_whenDeleteExercise() throws Exception {
            var id = randomId();

            mockMvc.perform(delete(BASE_URL + "/" + id.value()))
                    .andExpect(status().isNoContent());

            verify(deleteExerciseUseCase).execute(any());
        }

        @Test
        @DisplayName("should return 404 when exercise not found on delete")
        void shouldReturn404_whenExerciseNotFound() throws Exception {
            var id = randomId();
            doThrow(new ExerciseNotFoundException(id))
                    .when(deleteExerciseUseCase).execute(any());

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


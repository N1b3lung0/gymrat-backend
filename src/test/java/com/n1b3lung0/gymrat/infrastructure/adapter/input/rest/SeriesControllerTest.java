package com.n1b3lung0.gymrat.infrastructure.adapter.input.rest;

import com.n1b3lung0.gymrat.application.dto.SeriesDetailView;
import com.n1b3lung0.gymrat.application.dto.SeriesSummaryView;
import com.n1b3lung0.gymrat.application.port.input.command.DeleteSeriesUseCase;
import com.n1b3lung0.gymrat.application.port.input.command.RecordSeriesUseCase;
import com.n1b3lung0.gymrat.application.port.input.command.UpdateSeriesUseCase;
import com.n1b3lung0.gymrat.application.port.input.query.GetSeriesByIdUseCase;
import com.n1b3lung0.gymrat.application.port.input.query.ListSeriesByExerciseSeriesUseCase;
import com.n1b3lung0.gymrat.domain.exception.ExerciseSeriesNotFoundException;
import com.n1b3lung0.gymrat.domain.exception.SeriesNotFoundException;
import com.n1b3lung0.gymrat.domain.model.ExerciseSeriesId;
import com.n1b3lung0.gymrat.domain.model.RestTime;
import com.n1b3lung0.gymrat.domain.model.SeriesId;
import com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto.SeriesResponse;
import com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto.SeriesSummaryResponse;
import com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.mapper.SeriesRestMapper;
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

import java.math.BigDecimal;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller slice test for {@link SeriesController}.
 *
 * <p>Uses {@code MockMvcBuilders.standaloneSetup()} — same pattern as {@code ExerciseControllerTest}.
 * {@link GlobalExceptionHandler} is registered to validate error response codes.
 *
 * <p>Base URL: {@code /api/v1/workouts/{workoutId}/exercises/{exerciseSeriesId}/series}
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SeriesController")
class SeriesControllerTest {

    private static final UUID WORKOUT_ID          = UUID.randomUUID();
    private static final UUID EXERCISE_SERIES_ID  = UUID.randomUUID();
    private static final String BASE_URL =
            "/api/v1/workouts/" + WORKOUT_ID + "/exercises/" + EXERCISE_SERIES_ID + "/series";

    @Mock RecordSeriesUseCase               recordSeriesUseCase;
    @Mock UpdateSeriesUseCase               updateSeriesUseCase;
    @Mock DeleteSeriesUseCase               deleteSeriesUseCase;
    @Mock GetSeriesByIdUseCase              getSeriesByIdUseCase;
    @Mock ListSeriesByExerciseSeriesUseCase listSeriesUseCase;
    @Mock SeriesRestMapper                  mapper;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        var controller = new SeriesController(
                recordSeriesUseCase, updateSeriesUseCase, deleteSeriesUseCase,
                getSeriesByIdUseCase, listSeriesUseCase, mapper);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new JacksonJsonHttpMessageConverter())
                .build();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static SeriesId randomSeriesId() {
        return SeriesId.generate();
    }

    private static SeriesDetailView detailView(UUID id, UUID esId) {
        return new SeriesDetailView(id, 1, 10, null, 7, BigDecimal.valueOf(80), null, null, RestTime.SIXTY, esId);
    }

    private static SeriesResponse seriesResponse(UUID id, UUID esId) {
        return new SeriesResponse(id, 1, 10, null, 7, BigDecimal.valueOf(80), null, null, RestTime.SIXTY, esId);
    }

    private static SeriesSummaryResponse summaryResponse(UUID id) {
        return new SeriesSummaryResponse(id, 1, 10, 7, BigDecimal.valueOf(80), RestTime.SIXTY);
    }

    private static String recordRequestJson() {
        return """
                {
                  "repetitionsToDo": 10,
                  "intensity": 7,
                  "weight": 80,
                  "restTime": "SIXTY"
                }
                """;
    }

    private static String updateRequestJson() {
        return """
                {
                  "repetitionsToDo": 12,
                  "intensity": 8,
                  "weight": 85,
                  "restTime": "NINETY"
                }
                """;
    }

    // -------------------------------------------------------------------------
    // POST — record series
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("POST /api/v1/workouts/{workoutId}/exercises/{exerciseSeriesId}/series")
    class Record {

        @Test
        @DisplayName("should return 201 Created with Location header when series is recorded")
        void shouldReturn201_whenSeriesRecorded() throws Exception {
            var id = randomSeriesId();
            when(mapper.toRecordCommand(any(), any())).thenReturn(null);
            when(recordSeriesUseCase.execute(any())).thenReturn(id);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(recordRequestJson()))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", containsString(id.value().toString())));
        }

        @Test
        @DisplayName("should return 422 when repetitionsToDo is zero")
        void shouldReturn422_whenRepetitionsToDoIsZero() throws Exception {
            String body = """
                    {
                      "repetitionsToDo": 0,
                      "intensity": 7,
                      "restTime": "SIXTY"
                    }
                    """;

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isUnprocessableEntity());

            verify(recordSeriesUseCase, never()).execute(any());
        }

        @Test
        @DisplayName("should return 422 when restTime is missing")
        void shouldReturn422_whenRestTimeIsMissing() throws Exception {
            String body = """
                    {
                      "repetitionsToDo": 10,
                      "intensity": 7
                    }
                    """;

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isUnprocessableEntity());

            verify(recordSeriesUseCase, never()).execute(any());
        }

        @Test
        @DisplayName("should return 404 when exercise-series not found")
        void shouldReturn404_whenExerciseSeriesNotFound() throws Exception {
            when(mapper.toRecordCommand(any(), any())).thenReturn(null);
            when(recordSeriesUseCase.execute(any()))
                    .thenThrow(new ExerciseSeriesNotFoundException(ExerciseSeriesId.of(EXERCISE_SERIES_ID)));

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(recordRequestJson()))
                    .andExpect(status().isNotFound());
        }
    }

    // -------------------------------------------------------------------------
    // GET — list series
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GET /api/v1/workouts/{workoutId}/exercises/{exerciseSeriesId}/series")
    class ListSeries {

        @Test
        @DisplayName("should return 200 with list of series summaries")
        void shouldReturn200_whenSeriesExist() throws Exception {
            var id1 = UUID.randomUUID();
            var id2 = UUID.randomUUID();
            var views = List.of(
                    new SeriesSummaryView(id1, 1, 10, 7, BigDecimal.valueOf(80), RestTime.SIXTY),
                    new SeriesSummaryView(id2, 2, 8,  6, BigDecimal.valueOf(75), RestTime.NINETY)
            );
            when(listSeriesUseCase.execute(any())).thenReturn(views);
            when(mapper.toSummaryResponseList(views))
                    .thenReturn(List.of(summaryResponse(id1), summaryResponse(id2)));

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)));
        }

        @Test
        @DisplayName("should return 200 with empty list when no series exist")
        void shouldReturn200_whenNoSeriesExist() throws Exception {
            when(listSeriesUseCase.execute(any())).thenReturn(List.of());
            when(mapper.toSummaryResponseList(List.of())).thenReturn(List.of());

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    // -------------------------------------------------------------------------
    // GET /{seriesId}
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GET /api/v1/workouts/{workoutId}/exercises/{exerciseSeriesId}/series/{seriesId}")
    class GetById {

        @Test
        @DisplayName("should return 200 with series detail when found")
        void shouldReturn200_whenSeriesFound() throws Exception {
            var id   = randomSeriesId();
            var view = detailView(id.value(), EXERCISE_SERIES_ID);
            var resp = seriesResponse(id.value(), EXERCISE_SERIES_ID);
            when(getSeriesByIdUseCase.execute(any())).thenReturn(view);
            when(mapper.toResponse(view)).thenReturn(resp);

            mockMvc.perform(get(BASE_URL + "/" + id.value()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(id.value().toString())))
                    .andExpect(jsonPath("$.serialNumber", is(1)));
        }

        @Test
        @DisplayName("should return 404 when series not found")
        void shouldReturn404_whenSeriesNotFound() throws Exception {
            var id = randomSeriesId();
            when(getSeriesByIdUseCase.execute(any()))
                    .thenThrow(new SeriesNotFoundException(id));

            mockMvc.perform(get(BASE_URL + "/" + id.value()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should return 400 when seriesId is not a valid UUID")
        void shouldReturn400_whenSeriesIdIsInvalidUuid() throws Exception {
            mockMvc.perform(get(BASE_URL + "/not-a-uuid"))
                    .andExpect(status().isBadRequest());
        }
    }

    // -------------------------------------------------------------------------
    // PUT /{seriesId}
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("PUT /api/v1/workouts/{workoutId}/exercises/{exerciseSeriesId}/series/{seriesId}")
    class Update {

        @Test
        @DisplayName("should return 200 with updated series on success")
        void shouldReturn200_whenSeriesUpdated() throws Exception {
            var id   = randomSeriesId();
            var view = detailView(id.value(), EXERCISE_SERIES_ID);
            var resp = seriesResponse(id.value(), EXERCISE_SERIES_ID);
            when(mapper.toUpdateCommand(any(), any())).thenReturn(null);
            when(getSeriesByIdUseCase.execute(any())).thenReturn(view);
            when(mapper.toResponse(view)).thenReturn(resp);

            mockMvc.perform(put(BASE_URL + "/" + id.value())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateRequestJson()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(id.value().toString())));
        }

        @Test
        @DisplayName("should return 404 when series not found on update")
        void shouldReturn404_whenSeriesNotFound() throws Exception {
            var id = randomSeriesId();
            when(mapper.toUpdateCommand(any(), any())).thenReturn(null);
            doThrow(new SeriesNotFoundException(id))
                    .when(updateSeriesUseCase).execute(any());

            mockMvc.perform(put(BASE_URL + "/" + id.value())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateRequestJson()))
                    .andExpect(status().isNotFound());
        }
    }

    // -------------------------------------------------------------------------
    // DELETE /{seriesId}
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("DELETE /api/v1/workouts/{workoutId}/exercises/{exerciseSeriesId}/series/{seriesId}")
    class Delete {

        @Test
        @DisplayName("should return 204 No Content when series is deleted")
        void shouldReturn204_whenSeriesDeleted() throws Exception {
            var id = randomSeriesId();

            mockMvc.perform(delete(BASE_URL + "/" + id.value()))
                    .andExpect(status().isNoContent());

            verify(deleteSeriesUseCase).execute(any());
        }

        @Test
        @DisplayName("should return 404 when series not found on delete")
        void shouldReturn404_whenSeriesNotFound() throws Exception {
            var id = randomSeriesId();
            doThrow(new SeriesNotFoundException(id))
                    .when(deleteSeriesUseCase).execute(any());

            mockMvc.perform(delete(BASE_URL + "/" + id.value()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should return 400 when seriesId is not a valid UUID")
        void shouldReturn400_whenSeriesIdIsInvalidUuid() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/not-a-uuid"))
                    .andExpect(status().isBadRequest());
        }
    }
}


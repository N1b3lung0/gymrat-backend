package com.n1b3lung0.gymrat.application.query;

import com.n1b3lung0.gymrat.application.dto.ExerciseSeriesDetailView;
import com.n1b3lung0.gymrat.application.dto.ExerciseSeriesSummaryView;
import com.n1b3lung0.gymrat.application.dto.GetExerciseSeriesByIdQuery;
import com.n1b3lung0.gymrat.application.dto.ListExerciseSeriesByWorkoutQuery;
import com.n1b3lung0.gymrat.application.port.output.ExerciseSeriesQueryPort;
import com.n1b3lung0.gymrat.domain.exception.ExerciseSeriesNotFoundException;
import com.n1b3lung0.gymrat.domain.model.ExerciseSeriesId;
import com.n1b3lung0.gymrat.domain.model.WorkoutId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExerciseSeries Query Handlers")
class ExerciseSeriesQueryHandlersTest {

    @Mock ExerciseSeriesQueryPort exerciseSeriesQueryPort;

    GetExerciseSeriesByIdHandler        getByIdHandler;
    ListExerciseSeriesByWorkoutHandler  listHandler;

    @BeforeEach
    void setUp() {
        getByIdHandler = new GetExerciseSeriesByIdHandler(exerciseSeriesQueryPort);
        listHandler    = new ListExerciseSeriesByWorkoutHandler(exerciseSeriesQueryPort);
    }

    // -------------------------------------------------------------------------
    // GetExerciseSeriesByIdHandler
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GetExerciseSeriesByIdHandler")
    class GetById {

        @Test
        @DisplayName("should return ExerciseSeriesDetailView when found")
        void shouldReturnDetailView_whenFound() {
            var id   = ExerciseSeriesId.generate();
            var view = new ExerciseSeriesDetailView(
                    id.value(), UUID.randomUUID(), UUID.randomUUID(), List.of());
            when(exerciseSeriesQueryPort.findDetailById(id)).thenReturn(Optional.of(view));

            var result = getByIdHandler.execute(new GetExerciseSeriesByIdQuery(id));

            assertNotNull(result);
            assertEquals(id.value(), result.id());
        }

        @Test
        @DisplayName("should throw ExerciseSeriesNotFoundException when not found")
        void shouldThrow_whenNotFound() {
            var id = ExerciseSeriesId.generate();
            when(exerciseSeriesQueryPort.findDetailById(id)).thenReturn(Optional.empty());

            assertThrows(ExerciseSeriesNotFoundException.class,
                    () -> getByIdHandler.execute(new GetExerciseSeriesByIdQuery(id)));
        }

        @Test
        @DisplayName("should throw NullPointerException when query is null")
        void shouldThrow_whenQueryIsNull() {
            assertThrows(NullPointerException.class, () -> getByIdHandler.execute(null));
        }
    }

    // -------------------------------------------------------------------------
    // ListExerciseSeriesByWorkoutHandler
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("ListExerciseSeriesByWorkoutHandler")
    class ListByWorkout {

        @Test
        @DisplayName("should return list of summaries from query port")
        void shouldReturnSummaries() {
            var workoutId = WorkoutId.generate();
            var summaries = List.of(
                    new ExerciseSeriesSummaryView(UUID.randomUUID(), UUID.randomUUID(), 3));
            when(exerciseSeriesQueryPort.findAllSummariesByWorkoutId(workoutId)).thenReturn(summaries);

            var result = listHandler.execute(new ListExerciseSeriesByWorkoutQuery(workoutId));

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("should return empty list when no exercise-series exist")
        void shouldReturnEmptyList_whenNoResults() {
            var workoutId = WorkoutId.generate();
            when(exerciseSeriesQueryPort.findAllSummariesByWorkoutId(workoutId)).thenReturn(List.of());

            var result = listHandler.execute(new ListExerciseSeriesByWorkoutQuery(workoutId));

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should throw NullPointerException when query is null")
        void shouldThrow_whenQueryIsNull() {
            assertThrows(NullPointerException.class, () -> listHandler.execute(null));
        }
    }
}


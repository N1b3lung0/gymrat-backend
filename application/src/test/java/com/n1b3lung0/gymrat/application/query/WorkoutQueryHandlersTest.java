package com.n1b3lung0.gymrat.application.query;

import com.n1b3lung0.gymrat.application.dto.GetWorkoutByIdQuery;
import com.n1b3lung0.gymrat.application.dto.ListWorkoutsQuery;
import com.n1b3lung0.gymrat.application.dto.WorkoutDetailView;
import com.n1b3lung0.gymrat.application.dto.WorkoutSummaryView;
import com.n1b3lung0.gymrat.application.port.output.WorkoutQueryPort;
import com.n1b3lung0.gymrat.domain.exception.WorkoutNotFoundException;
import com.n1b3lung0.gymrat.domain.model.WorkoutId;
import com.n1b3lung0.gymrat.domain.repository.PageRequest;
import com.n1b3lung0.gymrat.domain.repository.PageResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Workout Query Handlers")
class WorkoutQueryHandlersTest {

    @Mock WorkoutQueryPort workoutQueryPort;

    GetWorkoutByIdHandler getByIdHandler;
    ListWorkoutsHandler   listHandler;

    @BeforeEach
    void setUp() {
        getByIdHandler = new GetWorkoutByIdHandler(workoutQueryPort);
        listHandler    = new ListWorkoutsHandler(workoutQueryPort);
    }

    // -------------------------------------------------------------------------
    // GetWorkoutByIdHandler
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GetWorkoutByIdHandler")
    class GetById {

        @Test
        @DisplayName("should return WorkoutDetailView when found")
        void shouldReturnDetailView_whenFound() {
            var id   = WorkoutId.generate();
            var view = new WorkoutDetailView(id.value(), Instant.now(), null, List.of());
            when(workoutQueryPort.findDetailById(id)).thenReturn(Optional.of(view));

            var result = getByIdHandler.execute(new GetWorkoutByIdQuery(id));

            assertNotNull(result);
            assertEquals(id.value(), result.id());
        }

        @Test
        @DisplayName("should throw WorkoutNotFoundException when not found")
        void shouldThrow_whenNotFound() {
            var id = WorkoutId.generate();
            when(workoutQueryPort.findDetailById(id)).thenReturn(Optional.empty());

            assertThrows(WorkoutNotFoundException.class,
                    () -> getByIdHandler.execute(new GetWorkoutByIdQuery(id)));
        }

        @Test
        @DisplayName("should throw NullPointerException when query is null")
        void shouldThrow_whenQueryIsNull() {
            assertThrows(NullPointerException.class, () -> getByIdHandler.execute(null));
        }
    }

    // -------------------------------------------------------------------------
    // ListWorkoutsHandler
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("ListWorkoutsHandler")
    class ListWorkouts {

        @Test
        @DisplayName("should return paginated results from query port")
        void shouldReturnPageResult() {
            var pageRequest = PageRequest.of(0, 10);
            var summary     = new WorkoutSummaryView(WorkoutId.generate().value(), Instant.now(), null, false);
            var expected    = new PageResult<>(List.of(summary), 0, 10, 1L);
            when(workoutQueryPort.findAll(pageRequest)).thenReturn(expected);

            var result = listHandler.execute(new ListWorkoutsQuery(pageRequest));

            assertEquals(1, result.content().size());
            assertEquals(1L, result.totalElements());
        }

        @Test
        @DisplayName("should return empty page when no workouts exist")
        void shouldReturnEmptyPage_whenNoResults() {
            var pageRequest = PageRequest.of(0, 10);
            when(workoutQueryPort.findAll(pageRequest))
                    .thenReturn(new PageResult<>(List.of(), 0, 10, 0L));

            var result = listHandler.execute(new ListWorkoutsQuery(pageRequest));

            assertTrue(result.content().isEmpty());
        }

        @Test
        @DisplayName("should throw NullPointerException when query is null")
        void shouldThrow_whenQueryIsNull() {
            assertThrows(NullPointerException.class, () -> listHandler.execute(null));
        }
    }
}


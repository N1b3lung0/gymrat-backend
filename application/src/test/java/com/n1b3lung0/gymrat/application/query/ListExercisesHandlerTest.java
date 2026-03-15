package com.n1b3lung0.gymrat.application.query;

import com.n1b3lung0.gymrat.application.dto.ExerciseSummaryView;
import com.n1b3lung0.gymrat.application.dto.ListExercisesQuery;
import com.n1b3lung0.gymrat.application.port.output.ExerciseQueryPort;
import com.n1b3lung0.gymrat.domain.model.Level;
import com.n1b3lung0.gymrat.domain.model.Muscle;
import com.n1b3lung0.gymrat.domain.model.Routine;
import com.n1b3lung0.gymrat.domain.repository.PageRequest;
import com.n1b3lung0.gymrat.domain.repository.PageResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListExercisesHandler")
class ListExercisesHandlerTest {

    @Mock ExerciseQueryPort exerciseQueryPort;

    ListExercisesHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ListExercisesHandler(exerciseQueryPort);
    }

    private static ExerciseSummaryView summaryView() {
        return new ExerciseSummaryView(
                UUID.randomUUID(),
                "Bench Press",
                Level.INTERMEDIATE,
                Muscle.CHEST,
                Set.of(Routine.PUSH)
        );
    }

    @Nested
    @DisplayName("execute() — happy path")
    class HappyPath {

        @Test
        @DisplayName("should return paginated results from query port")
        void shouldReturnPageResult() {
            var pageRequest = PageRequest.of(0, 10);
            var expected = new PageResult<>(List.of(summaryView()), 0, 10, 1L);
            when(exerciseQueryPort.findAll(pageRequest)).thenReturn(expected);

            var result = handler.execute(new ListExercisesQuery(pageRequest));

            assertNotNull(result);
            assertEquals(1, result.content().size());
            assertEquals(1L, result.totalElements());
        }

        @Test
        @DisplayName("should delegate to ExerciseQueryPort with the given PageRequest")
        void shouldDelegateToQueryPort() {
            var pageRequest = PageRequest.of(0, 20, "name", true);
            when(exerciseQueryPort.findAll(pageRequest))
                    .thenReturn(new PageResult<>(List.of(), 0, 20, 0L));

            handler.execute(new ListExercisesQuery(pageRequest));

            verify(exerciseQueryPort).findAll(pageRequest);
        }

        @Test
        @DisplayName("should return empty page when no exercises exist")
        void shouldReturnEmptyPage_whenNoResults() {
            var pageRequest = PageRequest.of(0, 10);
            when(exerciseQueryPort.findAll(pageRequest))
                    .thenReturn(new PageResult<>(List.of(), 0, 10, 0L));

            var result = handler.execute(new ListExercisesQuery(pageRequest));

            assertTrue(result.content().isEmpty());
            assertEquals(0L, result.totalElements());
        }
    }

    @Nested
    @DisplayName("execute() — error cases")
    class ErrorCases {

        @Test
        @DisplayName("should throw NullPointerException when query is null")
        void shouldThrow_whenQueryIsNull() {
            assertThrows(NullPointerException.class, () -> handler.execute(null));
        }
    }
}


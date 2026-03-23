package com.n1b3lung0.gymrat.application.query;

import com.n1b3lung0.gymrat.application.dto.ExerciseDetailView;
import com.n1b3lung0.gymrat.application.dto.GetExerciseByIdQuery;
import com.n1b3lung0.gymrat.application.port.output.ExerciseQueryPort;
import com.n1b3lung0.gymrat.domain.exception.ExerciseNotFoundException;
import com.n1b3lung0.gymrat.domain.model.ExerciseId;
import com.n1b3lung0.gymrat.domain.model.Level;
import com.n1b3lung0.gymrat.domain.model.Muscle;
import com.n1b3lung0.gymrat.domain.model.Routine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetExerciseByIdHandler")
class GetExerciseByIdHandlerTest {

    @Mock ExerciseQueryPort exerciseQueryPort;

    GetExerciseByIdHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GetExerciseByIdHandler(exerciseQueryPort);
    }

    private static ExerciseDetailView stubView(ExerciseId id) {
        return new ExerciseDetailView(
                id.value(),
                "Bench Press",
                "Classic chest press",
                Level.INTERMEDIATE,
                Set.of(Routine.PUSH),
                Muscle.CHEST,
                Set.of(Muscle.TRICEPS),
                null,
                null
        );
    }

    @Nested
    @DisplayName("execute() — happy path")
    class HappyPath {

        @Test
        @DisplayName("should return ExerciseDetailView when exercise exists")
        void shouldReturnDetailView_whenFound() {
            var id = ExerciseId.generate();
            var view = stubView(id);
            when(exerciseQueryPort.findDetailById(id)).thenReturn(Optional.of(view));

            var result = handler.execute(new GetExerciseByIdQuery(id));

            assertNotNull(result);
            assertEquals("Bench Press", result.name());
            assertEquals(id.value(), result.id());
        }

        @Test
        @DisplayName("should delegate to ExerciseQueryPort")
        void shouldDelegateToQueryPort() {
            var id = ExerciseId.generate();
            when(exerciseQueryPort.findDetailById(id)).thenReturn(Optional.of(stubView(id)));

            handler.execute(new GetExerciseByIdQuery(id));

            verify(exerciseQueryPort).findDetailById(id);
        }
    }

    @Nested
    @DisplayName("execute() — error cases")
    class ErrorCases {

        @Test
        @DisplayName("should throw ExerciseNotFoundException when exercise does not exist")
        void shouldThrow_whenNotFound() {
            var id = ExerciseId.generate();
            when(exerciseQueryPort.findDetailById(id)).thenReturn(Optional.empty());

            assertThrows(ExerciseNotFoundException.class,
                    () -> handler.execute(new GetExerciseByIdQuery(id)));
        }

        @Test
        @DisplayName("should throw NullPointerException when query is null")
        void shouldThrow_whenQueryIsNull() {
            assertThrows(NullPointerException.class, () -> handler.execute(null));
        }
    }
}


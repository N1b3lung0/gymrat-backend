package com.n1b3lung0.gymrat.application.command;

import com.n1b3lung0.gymrat.application.dto.DeleteExerciseCommand;
import com.n1b3lung0.gymrat.application.port.output.DomainEventPublisherPort;
import com.n1b3lung0.gymrat.domain.event.ExerciseDeleted;
import com.n1b3lung0.gymrat.domain.exception.ExerciseNotFoundException;
import com.n1b3lung0.gymrat.domain.model.Exercise;
import com.n1b3lung0.gymrat.domain.model.ExerciseId;
import com.n1b3lung0.gymrat.domain.model.Level;
import com.n1b3lung0.gymrat.domain.model.Muscle;
import com.n1b3lung0.gymrat.domain.model.Routine;
import com.n1b3lung0.gymrat.domain.repository.ExerciseRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteExerciseHandler")
class DeleteExerciseHandlerTest {

    @Mock ExerciseRepositoryPort exerciseRepository;
    @Mock DomainEventPublisherPort eventPublisher;

    DeleteExerciseHandler handler;

    @BeforeEach
    void setUp() {
        handler = new DeleteExerciseHandler(exerciseRepository, eventPublisher);
    }

    private static Exercise existingExercise() {
        return Exercise.builder()
                .name("Deadlift")
                .level(Level.ADVANCED)
                .routines(Set.of(Routine.PULL))
                .primaryMuscle(Muscle.BACK)
                .build();
    }

    @Nested
    @DisplayName("execute() — happy path")
    class HappyPath {

        @BeforeEach
        void stub() {
            when(exerciseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        }

        @Test
        @DisplayName("should publish ExerciseDeleted event after save")
        void shouldPublishExerciseDeletedEvent() {
            var exercise = existingExercise();
            exercise.pullDomainEvents(); // drain creation event
            when(exerciseRepository.findById(exercise.getId())).thenReturn(Optional.of(exercise));

            handler.execute(new DeleteExerciseCommand(exercise.getId()));

            var captor = ArgumentCaptor.forClass(Object.class);
            verify(eventPublisher).publish(captor.capture());
            assertInstanceOf(ExerciseDeleted.class, captor.getValue());
        }

        @Test
        @DisplayName("should persist soft-deleted exercise")
        void shouldSaveSoftDeletedExercise() {
            var exercise = existingExercise();
            when(exerciseRepository.findById(exercise.getId())).thenReturn(Optional.of(exercise));

            handler.execute(new DeleteExerciseCommand(exercise.getId()));

            verify(exerciseRepository).save(exercise);
        }

        @Test
        @DisplayName("should mark exercise as inactive after delete")
        void shouldMarkExerciseInactive() {
            var exercise = existingExercise();
            when(exerciseRepository.findById(exercise.getId())).thenReturn(Optional.of(exercise));

            handler.execute(new DeleteExerciseCommand(exercise.getId()));

            assertFalse(exercise.getAuditFields().active());
            assertNotNull(exercise.getAuditFields().deletedAt());
        }
    }

    @Nested
    @DisplayName("execute() — error cases")
    class ErrorCases {

        @Test
        @DisplayName("should throw ExerciseNotFoundException when exercise does not exist")
        void shouldThrow_whenExerciseNotFound() {
            var id = ExerciseId.generate();
            when(exerciseRepository.findById(id)).thenReturn(Optional.empty());

            assertThrows(ExerciseNotFoundException.class,
                    () -> handler.execute(new DeleteExerciseCommand(id)));

            verify(exerciseRepository, never()).save(any());
            verify(eventPublisher, never()).publish(any());
        }

        @Test
        @DisplayName("should throw NullPointerException when command is null")
        void shouldThrow_whenCommandIsNull() {
            assertThrows(NullPointerException.class, () -> handler.execute(null));
        }
    }
}


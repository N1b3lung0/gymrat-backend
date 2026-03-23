package com.n1b3lung0.gymrat.application.command;

import com.n1b3lung0.gymrat.application.dto.UpdateExerciseCommand;
import com.n1b3lung0.gymrat.application.port.output.DomainEventPublisherPort;
import com.n1b3lung0.gymrat.domain.event.ExerciseUpdated;
import com.n1b3lung0.gymrat.domain.exception.DuplicateExerciseNameException;
import com.n1b3lung0.gymrat.domain.exception.ExerciseNotFoundException;
import com.n1b3lung0.gymrat.domain.model.Exercise;
import com.n1b3lung0.gymrat.domain.model.ExerciseId;
import com.n1b3lung0.gymrat.domain.model.Level;
import com.n1b3lung0.gymrat.domain.model.Muscle;
import com.n1b3lung0.gymrat.domain.model.Routine;
import com.n1b3lung0.gymrat.domain.repository.ExerciseRepositoryPort;
import com.n1b3lung0.gymrat.domain.repository.MediaRepositoryPort;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateExerciseHandler")
class UpdateExerciseHandlerTest {

    @Mock ExerciseRepositoryPort exerciseRepository;
    @Mock MediaRepositoryPort    mediaRepository;
    @Mock DomainEventPublisherPort eventPublisher;

    UpdateExerciseHandler handler;

    @BeforeEach
    void setUp() {
        handler = new UpdateExerciseHandler(exerciseRepository, mediaRepository, eventPublisher);
    }

    private static Exercise existingExercise() {
        return Exercise.builder()
                .name("Bench Press")
                .description("Classic chest press")
                .level(Level.INTERMEDIATE)
                .routines(Set.of(Routine.PUSH))
                .primaryMuscle(Muscle.CHEST)
                .secondaryMuscles(Set.of(Muscle.TRICEPS))
                .build();
    }

    private static UpdateExerciseCommand commandFor(Exercise exercise, String newName) {
        return new UpdateExerciseCommand(
                exercise.getId(),
                newName,
                "Updated description",
                Level.ADVANCED,
                Set.of(Routine.PUSH, Routine.UPPERBODY),
                Muscle.CHEST,
                Set.of(Muscle.SHOULDERS),
                null, null, null,
                null, null, null
        );
    }

    @Nested
    @DisplayName("execute() — happy path")
    class HappyPath {

        @BeforeEach
        void stub() {
            when(exerciseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        }

        @Test
        @DisplayName("should publish ExerciseUpdated event after save")
        void shouldPublishExerciseUpdatedEvent() {
            var exercise = existingExercise();
            exercise.pullDomainEvents(); // drain creation event
            when(exerciseRepository.findById(exercise.getId())).thenReturn(Optional.of(exercise));
            when(exerciseRepository.existsByName("Incline Bench Press")).thenReturn(false);

            handler.execute(commandFor(exercise, "Incline Bench Press"));

            var captor = ArgumentCaptor.forClass(Object.class);
            verify(eventPublisher).publish(captor.capture());
            assertInstanceOf(ExerciseUpdated.class, captor.getValue());
        }

        @Test
        @DisplayName("should persist updated exercise")
        void shouldSaveUpdatedExercise() {
            var exercise = existingExercise();
            when(exerciseRepository.findById(exercise.getId())).thenReturn(Optional.of(exercise));
            when(exerciseRepository.existsByName("Incline Bench Press")).thenReturn(false);

            handler.execute(commandFor(exercise, "Incline Bench Press"));

            verify(exerciseRepository).save(exercise);
        }

        @Test
        @DisplayName("should not check for duplicate when name is unchanged")
        void shouldSkipDuplicateCheck_whenNameUnchanged() {
            var exercise = existingExercise();
            when(exerciseRepository.findById(exercise.getId())).thenReturn(Optional.of(exercise));

            handler.execute(commandFor(exercise, "Bench Press")); // same name

            verify(exerciseRepository, never()).existsByName(anyString());
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

            var command = new UpdateExerciseCommand(
                    id, "Name", null, Level.BEGINNER,
                    Set.of(Routine.LEG), Muscle.QUADRICEPS, Set.of(),
                    null, null, null, null, null, null
            );

            assertThrows(ExerciseNotFoundException.class, () -> handler.execute(command));
            verify(exerciseRepository, never()).save(any());
            verify(eventPublisher, never()).publish(any());
        }

        @Test
        @DisplayName("should throw DuplicateExerciseNameException when new name already exists")
        void shouldThrow_whenNewNameAlreadyExists() {
            var exercise = existingExercise();
            when(exerciseRepository.findById(exercise.getId())).thenReturn(Optional.of(exercise));
            when(exerciseRepository.existsByName("Squat")).thenReturn(true);

            assertThrows(DuplicateExerciseNameException.class,
                    () -> handler.execute(commandFor(exercise, "Squat")));

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


package com.n1b3lung0.gymrat.application.command;

import com.n1b3lung0.gymrat.application.dto.CreateExerciseCommand;
import com.n1b3lung0.gymrat.application.port.output.DomainEventPublisherPort;
import com.n1b3lung0.gymrat.application.port.output.MetricsPort;
import com.n1b3lung0.gymrat.domain.event.ExerciseCreated;
import com.n1b3lung0.gymrat.domain.exception.DuplicateExerciseNameException;
import com.n1b3lung0.gymrat.domain.model.Level;
import com.n1b3lung0.gymrat.domain.model.Media;
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
import java.util.UUID;

import static com.n1b3lung0.gymrat.application.command.CreateExerciseHandler.EXERCISES_CREATED_METRIC;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateExerciseHandler")
class CreateExerciseHandlerTest {

    @Mock ExerciseRepositoryPort exerciseRepository;
    @Mock MediaRepositoryPort    mediaRepository;
    @Mock DomainEventPublisherPort eventPublisher;
    @Mock MetricsPort            metrics;

    CreateExerciseHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CreateExerciseHandler(exerciseRepository, mediaRepository, eventPublisher, metrics);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static CreateExerciseCommand minimalCommand() {
        return new CreateExerciseCommand(
                "Bench Press",
                "Classic chest press",
                Level.INTERMEDIATE,
                Set.of(Routine.PUSH),
                Muscle.CHEST,
                Set.of(Muscle.TRICEPS),
                null, null, null,   // no image
                null, null, null,   // no video
                UUID.randomUUID()
        );
    }

    // -------------------------------------------------------------------------
    // Happy path
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("execute() — happy path")
    class HappyPath {

        @BeforeEach
        void stubSave() {
            when(exerciseRepository.existsByName(anyString())).thenReturn(false);
            when(exerciseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        }

        @Test
        @DisplayName("should return a non-null ExerciseId")
        void shouldReturnExerciseId() {
            var id = handler.execute(minimalCommand());

            assertNotNull(id);
            assertNotNull(id.value());
        }

        @Test
        @DisplayName("should publish ExerciseCreated event after save")
        void shouldPublishExerciseCreatedEvent() {
            handler.execute(minimalCommand());

            var captor = ArgumentCaptor.forClass(Object.class);
            verify(eventPublisher).publish(captor.capture());
            assertInstanceOf(ExerciseCreated.class, captor.getValue());
        }

        @Test
        @DisplayName("should increment exercises.created.total metric")
        void shouldIncrementMetricCounter() {
            handler.execute(minimalCommand());

            verify(metrics).increment(EXERCISES_CREATED_METRIC);
        }

        @Test
        @DisplayName("should persist the exercise via repository")
        void shouldSaveExercise() {
            handler.execute(minimalCommand());

            verify(exerciseRepository).save(any());
        }

        @Test
        @DisplayName("should resolve existing media by URL when imageUrl is provided")
        void shouldReuseExistingMedia_whenUrlAlreadyKnown() {
            var existingImage = Media.of("photo", "desc", "https://cdn.example.com/img.jpg");
            when(mediaRepository.findByUrl("https://cdn.example.com/img.jpg"))
                    .thenReturn(Optional.of(existingImage));

            var commandWithImage = new CreateExerciseCommand(
                    "Squat", null, Level.BEGINNER,
                    Set.of(Routine.LEG), Muscle.QUADRICEPS, Set.of(),
                    "photo", "desc", "https://cdn.example.com/img.jpg",
                    null, null, null,
                    UUID.randomUUID()
            );

            handler.execute(commandWithImage);

            verify(mediaRepository, never()).save(any());
        }

        @Test
        @DisplayName("should save new media when URL is not yet known")
        void shouldSaveNewMedia_whenUrlIsNew() {
            when(mediaRepository.findByUrl(anyString())).thenReturn(Optional.empty());
            when(mediaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            var commandWithImage = new CreateExerciseCommand(
                    "Deadlift", null, Level.ADVANCED,
                    Set.of(Routine.PULL), Muscle.BACK, Set.of(),
                    "photo", "desc", "https://cdn.example.com/dl.jpg",
                    null, null, null,
                    UUID.randomUUID()
            );

            handler.execute(commandWithImage);

            verify(mediaRepository).save(any());
        }
    }

    // -------------------------------------------------------------------------
    // Error cases
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("execute() — error cases")
    class ErrorCases {

        @Test
        @DisplayName("should throw DuplicateExerciseNameException when name already exists")
        void shouldThrow_whenNameAlreadyExists() {
            when(exerciseRepository.existsByName("Bench Press")).thenReturn(true);

            assertThrows(DuplicateExerciseNameException.class,
                    () -> handler.execute(minimalCommand()));

            verify(exerciseRepository, never()).save(any());
            verify(eventPublisher, never()).publish(any());
            verify(metrics, never()).increment(any());
        }

        @Test
        @DisplayName("should throw NullPointerException when command is null")
        void shouldThrow_whenCommandIsNull() {
            assertThrows(NullPointerException.class, () -> handler.execute(null));
        }
    }
}


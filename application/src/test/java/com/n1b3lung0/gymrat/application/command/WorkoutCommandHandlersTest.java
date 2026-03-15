package com.n1b3lung0.gymrat.application.command;

import com.n1b3lung0.gymrat.application.dto.CreateWorkoutCommand;
import com.n1b3lung0.gymrat.application.dto.DeleteWorkoutCommand;
import com.n1b3lung0.gymrat.application.dto.FinishWorkoutCommand;
import com.n1b3lung0.gymrat.application.port.output.DomainEventPublisherPort;
import com.n1b3lung0.gymrat.domain.event.WorkoutFinished;
import com.n1b3lung0.gymrat.domain.event.WorkoutStarted;
import com.n1b3lung0.gymrat.domain.exception.WorkoutAlreadyFinishedException;
import com.n1b3lung0.gymrat.domain.exception.WorkoutNotFoundException;
import com.n1b3lung0.gymrat.domain.model.Workout;
import com.n1b3lung0.gymrat.domain.model.WorkoutId;
import com.n1b3lung0.gymrat.domain.repository.WorkoutRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Workout Command Handlers")
class WorkoutCommandHandlersTest {

    @Mock WorkoutRepositoryPort workoutRepository;
    @Mock DomainEventPublisherPort eventPublisher;

    CreateWorkoutHandler createHandler;
    FinishWorkoutHandler finishHandler;
    DeleteWorkoutHandler deleteHandler;

    @BeforeEach
    void setUp() {
        createHandler = new CreateWorkoutHandler(workoutRepository, eventPublisher);
        finishHandler = new FinishWorkoutHandler(workoutRepository, eventPublisher);
        deleteHandler = new DeleteWorkoutHandler(workoutRepository, eventPublisher);
    }

    // -------------------------------------------------------------------------
    // CreateWorkoutHandler
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("CreateWorkoutHandler")
    class Create {

        @Test
        @DisplayName("should return a non-null WorkoutId")
        void shouldReturnWorkoutId() {
            when(workoutRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            var id = createHandler.execute(new CreateWorkoutCommand(Instant.now()));
            assertNotNull(id);
            assertNotNull(id.value());
        }

        @Test
        @DisplayName("should publish WorkoutStarted event after save")
        void shouldPublishWorkoutStartedEvent() {
            when(workoutRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            createHandler.execute(new CreateWorkoutCommand(Instant.now()));

            var captor = ArgumentCaptor.forClass(Object.class);
            verify(eventPublisher).publish(captor.capture());
            assertInstanceOf(WorkoutStarted.class, captor.getValue());
        }

        @Test
        @DisplayName("should persist workout via repository")
        void shouldSaveWorkout() {
            when(workoutRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            createHandler.execute(new CreateWorkoutCommand(Instant.now()));
            verify(workoutRepository).save(any());
        }

        @Test
        @DisplayName("should throw NullPointerException when command is null")
        void shouldThrow_whenCommandIsNull() {
            assertThrows(NullPointerException.class, () -> createHandler.execute(null));
        }
    }

    // -------------------------------------------------------------------------
    // FinishWorkoutHandler
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("FinishWorkoutHandler")
    class Finish {

        @Test
        @DisplayName("should publish WorkoutFinished event after save")
        void shouldPublishWorkoutFinishedEvent() {
            when(workoutRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            var start   = Instant.now().minusSeconds(3600);
            var workout = Workout.create(start);
            workout.pullDomainEvents();
            when(workoutRepository.findById(workout.getId())).thenReturn(Optional.of(workout));

            finishHandler.execute(new FinishWorkoutCommand(workout.getId(), Instant.now()));

            var captor = ArgumentCaptor.forClass(Object.class);
            verify(eventPublisher).publish(captor.capture());
            assertInstanceOf(WorkoutFinished.class, captor.getValue());
        }

        @Test
        @DisplayName("should throw WorkoutNotFoundException when workout does not exist")
        void shouldThrow_whenWorkoutNotFound() {
            var id = WorkoutId.generate();
            when(workoutRepository.findById(id)).thenReturn(Optional.empty());

            assertThrows(WorkoutNotFoundException.class,
                    () -> finishHandler.execute(new FinishWorkoutCommand(id, Instant.now())));

            verify(workoutRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw WorkoutAlreadyFinishedException when already finished")
        void shouldThrow_whenAlreadyFinished() {
            var start   = Instant.now().minusSeconds(3600);
            var workout = Workout.create(start);
            workout.finish(Instant.now());
            workout.pullDomainEvents();
            when(workoutRepository.findById(workout.getId())).thenReturn(Optional.of(workout));

            assertThrows(WorkoutAlreadyFinishedException.class,
                    () -> finishHandler.execute(
                            new FinishWorkoutCommand(workout.getId(), Instant.now())));
        }
    }

    // -------------------------------------------------------------------------
    // DeleteWorkoutHandler
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("DeleteWorkoutHandler")
    class Delete {

        @Test
        @DisplayName("should call deleteById on repository")
        void shouldCallDeleteById() {
            var workout = Workout.create(Instant.now());
            when(workoutRepository.findById(workout.getId())).thenReturn(Optional.of(workout));

            deleteHandler.execute(new DeleteWorkoutCommand(workout.getId()));

            verify(workoutRepository).deleteById(workout.getId());
        }

        @Test
        @DisplayName("should throw WorkoutNotFoundException when workout does not exist")
        void shouldThrow_whenWorkoutNotFound() {
            var id = WorkoutId.generate();
            when(workoutRepository.findById(id)).thenReturn(Optional.empty());

            assertThrows(WorkoutNotFoundException.class,
                    () -> deleteHandler.execute(new DeleteWorkoutCommand(id)));

            verify(workoutRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("should throw NullPointerException when command is null")
        void shouldThrow_whenCommandIsNull() {
            assertThrows(NullPointerException.class, () -> deleteHandler.execute(null));
        }
    }
}


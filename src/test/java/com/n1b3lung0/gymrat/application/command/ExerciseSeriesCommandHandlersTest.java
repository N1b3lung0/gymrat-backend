package com.n1b3lung0.gymrat.application.command;

import com.n1b3lung0.gymrat.application.dto.AddExerciseToWorkoutCommand;
import com.n1b3lung0.gymrat.application.dto.RemoveExerciseFromWorkoutCommand;
import com.n1b3lung0.gymrat.application.port.output.DomainEventPublisherPort;
import com.n1b3lung0.gymrat.domain.exception.ExerciseNotFoundException;
import com.n1b3lung0.gymrat.domain.exception.ExerciseSeriesNotFoundException;
import com.n1b3lung0.gymrat.domain.exception.WorkoutNotFoundException;
import com.n1b3lung0.gymrat.domain.model.Exercise;
import com.n1b3lung0.gymrat.domain.model.ExerciseSeriesId;
import com.n1b3lung0.gymrat.domain.model.Level;
import com.n1b3lung0.gymrat.domain.model.Muscle;
import com.n1b3lung0.gymrat.domain.model.Routine;
import com.n1b3lung0.gymrat.domain.model.Workout;
import com.n1b3lung0.gymrat.domain.model.WorkoutId;
import com.n1b3lung0.gymrat.domain.repository.ExerciseRepositoryPort;
import com.n1b3lung0.gymrat.domain.repository.ExerciseSeriesRepositoryPort;
import com.n1b3lung0.gymrat.domain.repository.WorkoutRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExerciseSeries Command Handlers")
class ExerciseSeriesCommandHandlersTest {

    @Mock WorkoutRepositoryPort          workoutRepository;
    @Mock ExerciseRepositoryPort         exerciseRepository;
    @Mock ExerciseSeriesRepositoryPort   exerciseSeriesRepository;
    @Mock DomainEventPublisherPort       eventPublisher;

    AddExerciseToWorkoutHandler      addHandler;
    RemoveExerciseFromWorkoutHandler removeHandler;

    @BeforeEach
    void setUp() {
        addHandler    = new AddExerciseToWorkoutHandler(
                workoutRepository, exerciseRepository, exerciseSeriesRepository, eventPublisher);
        removeHandler = new RemoveExerciseFromWorkoutHandler(exerciseSeriesRepository);
    }

    private static Exercise exercise() {
        return Exercise.builder()
                .name("Bench Press")
                .level(Level.INTERMEDIATE)
                .routines(Set.of(Routine.PUSH))
                .primaryMuscle(Muscle.CHEST)
                .build();
    }

    // -------------------------------------------------------------------------
    // AddExerciseToWorkoutHandler
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("AddExerciseToWorkoutHandler")
    class Add {

        @Test
        @DisplayName("should return a non-null ExerciseSeriesId")
        void shouldReturnExerciseSeriesId() {
            var workout  = Workout.create(Instant.now());
            var exercise = exercise();
            when(workoutRepository.findById(workout.getId())).thenReturn(Optional.of(workout));
            when(exerciseRepository.findById(exercise.getId())).thenReturn(Optional.of(exercise));
            when(exerciseSeriesRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(workoutRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(exerciseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            var id = addHandler.execute(
                    new AddExerciseToWorkoutCommand(workout.getId(), exercise.getId()));

            assertNotNull(id);
            assertNotNull(id.value());
        }

        @Test
        @DisplayName("should persist ExerciseSeries, Workout and Exercise")
        void shouldPersistAllThreeAggregates() {
            var workout  = Workout.create(Instant.now());
            var exercise = exercise();
            when(workoutRepository.findById(workout.getId())).thenReturn(Optional.of(workout));
            when(exerciseRepository.findById(exercise.getId())).thenReturn(Optional.of(exercise));
            when(exerciseSeriesRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(workoutRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(exerciseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            addHandler.execute(
                    new AddExerciseToWorkoutCommand(workout.getId(), exercise.getId()));

            verify(exerciseSeriesRepository).save(any());
            verify(workoutRepository).save(workout);
            verify(exerciseRepository).save(exercise);
        }

        @Test
        @DisplayName("should register ExerciseSeriesId on Workout")
        void shouldRegisterExerciseSeriesOnWorkout() {
            var workout  = Workout.create(Instant.now());
            var exercise = exercise();
            when(workoutRepository.findById(workout.getId())).thenReturn(Optional.of(workout));
            when(exerciseRepository.findById(exercise.getId())).thenReturn(Optional.of(exercise));
            when(exerciseSeriesRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(workoutRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(exerciseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            var id = addHandler.execute(
                    new AddExerciseToWorkoutCommand(workout.getId(), exercise.getId()));

            assertTrue(workout.getExerciseSeriesIds().contains(id));
        }

        @Test
        @DisplayName("should throw WorkoutNotFoundException when workout does not exist")
        void shouldThrow_whenWorkoutNotFound() {
            var workoutId  = WorkoutId.generate();
            var exerciseId = exercise().getId();
            when(workoutRepository.findById(workoutId)).thenReturn(Optional.empty());

            assertThrows(WorkoutNotFoundException.class,
                    () -> addHandler.execute(
                            new AddExerciseToWorkoutCommand(workoutId, exerciseId)));

            verify(exerciseSeriesRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw ExerciseNotFoundException when exercise does not exist")
        void shouldThrow_whenExerciseNotFound() {
            var workout  = Workout.create(Instant.now());
            var exercise = exercise();
            when(workoutRepository.findById(workout.getId())).thenReturn(Optional.of(workout));
            when(exerciseRepository.findById(exercise.getId())).thenReturn(Optional.empty());

            assertThrows(ExerciseNotFoundException.class,
                    () -> addHandler.execute(
                            new AddExerciseToWorkoutCommand(workout.getId(), exercise.getId())));

            verify(exerciseSeriesRepository, never()).save(any());
        }
    }

    // -------------------------------------------------------------------------
    // RemoveExerciseFromWorkoutHandler
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("RemoveExerciseFromWorkoutHandler")
    class Remove {

        @Test
        @DisplayName("should call deleteById on repository")
        void shouldCallDeleteById() {
            var id = ExerciseSeriesId.generate();
            when(exerciseSeriesRepository.findById(id))
                    .thenReturn(Optional.of(mock(com.n1b3lung0.gymrat.domain.model.ExerciseSeries.class)));

            removeHandler.execute(new RemoveExerciseFromWorkoutCommand(id));

            verify(exerciseSeriesRepository).deleteById(id);
        }

        @Test
        @DisplayName("should throw ExerciseSeriesNotFoundException when not found")
        void shouldThrow_whenNotFound() {
            var id = ExerciseSeriesId.generate();
            when(exerciseSeriesRepository.findById(id)).thenReturn(Optional.empty());

            assertThrows(ExerciseSeriesNotFoundException.class,
                    () -> removeHandler.execute(new RemoveExerciseFromWorkoutCommand(id)));

            verify(exerciseSeriesRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("should throw NullPointerException when command is null")
        void shouldThrow_whenCommandIsNull() {
            assertThrows(NullPointerException.class, () -> removeHandler.execute(null));
        }
    }
}


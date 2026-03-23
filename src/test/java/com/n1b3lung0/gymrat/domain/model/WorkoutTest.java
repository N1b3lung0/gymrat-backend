package com.n1b3lung0.gymrat.domain.model;

import com.n1b3lung0.gymrat.domain.event.WorkoutFinished;
import com.n1b3lung0.gymrat.domain.event.WorkoutStarted;
import com.n1b3lung0.gymrat.domain.exception.WorkoutAlreadyFinishedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Workout aggregate")
class WorkoutTest {

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static Workout validWorkout() {
        return Workout.create(Instant.now().minusSeconds(3600));
    }

    // -------------------------------------------------------------------------
    // Creation
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("should generate a non-null UUID id on creation")
        void shouldGenerateId_onCreation() {
            var workout = validWorkout();

            assertNotNull(workout.getId());
            assertNotNull(workout.getId().value());
        }

        @Test
        @DisplayName("should emit WorkoutStarted event when created")
        void shouldEmitWorkoutStartedEvent_whenCreated() {
            var start   = Instant.now().minusSeconds(600);
            var workout = Workout.create(start);

            var events = workout.pullDomainEvents();

            assertEquals(1, events.size());
            assertInstanceOf(WorkoutStarted.class, events.getFirst());

            var event = (WorkoutStarted) events.getFirst();
            assertEquals(workout.getId(), event.workoutId());
            assertEquals(start, event.startWorkout());
            assertNotNull(event.occurredOn());
        }

        @Test
        @DisplayName("should initialise audit fields with active=true on creation")
        void shouldInitialiseAuditFields_onCreation() {
            var workout = validWorkout();

            var audit = workout.getAuditFields();

            assertTrue(audit.active());
            assertNotNull(audit.createdAt());
            assertNotNull(audit.createdBy());
            assertNull(audit.updatedAt());
            assertNull(audit.deletedAt());
            assertNull(audit.deletedBy());
        }

        @Test
        @DisplayName("should start with no exercise series")
        void shouldStartWithNoExerciseSeries() {
            var workout = validWorkout();

            assertTrue(workout.getExerciseSeriesIds().isEmpty());
        }

        @Test
        @DisplayName("should not be finished on creation")
        void shouldNotBeFinished_onCreation() {
            var workout = validWorkout();

            assertFalse(workout.isFinished());
            assertNull(workout.getEndWorkout());
        }

        @Test
        @DisplayName("should throw when startWorkout is null")
        void shouldThrow_whenStartWorkoutIsNull() {
            assertThrows(NullPointerException.class, () -> Workout.create(null));
        }

        @Test
        @DisplayName("should drain event list after pullDomainEvents()")
        void shouldDrainEventList_afterPull() {
            var workout = validWorkout();
            workout.pullDomainEvents();

            var secondPull = workout.pullDomainEvents();

            assertTrue(secondPull.isEmpty());
        }
    }

    // -------------------------------------------------------------------------
    // Finish
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("finish()")
    class Finish {

        @Test
        @DisplayName("should emit WorkoutFinished event when finished")
        void shouldEmitWorkoutFinishedEvent_whenFinished() {
            var workout = validWorkout();
            workout.pullDomainEvents(); // drain creation event

            var end = Instant.now();
            workout.finish(end);

            var events = workout.pullDomainEvents();
            assertEquals(1, events.size());
            assertInstanceOf(WorkoutFinished.class, events.getFirst());

            var event = (WorkoutFinished) events.getFirst();
            assertEquals(workout.getId(), event.workoutId());
            assertEquals(end, event.endWorkout());
            assertNotNull(event.occurredOn());
        }

        @Test
        @DisplayName("should mark workout as finished")
        void shouldMarkAsFinished_whenFinished() {
            var workout = validWorkout();

            workout.finish(Instant.now());

            assertTrue(workout.isFinished());
            assertNotNull(workout.getEndWorkout());
        }

        @Test
        @DisplayName("should throw WorkoutAlreadyFinishedException when already finished")
        void shouldThrow_whenAlreadyFinished() {
            var workout = validWorkout();
            workout.finish(Instant.now());

            assertThrows(WorkoutAlreadyFinishedException.class,
                    () -> workout.finish(Instant.now().plusSeconds(60)));
        }

        @Test
        @DisplayName("should throw when endWorkout is before startWorkout")
        void shouldThrow_whenEndBeforeStart() {
            var start   = Instant.now();
            var workout = Workout.create(start);

            assertThrows(IllegalArgumentException.class,
                    () -> workout.finish(start.minusSeconds(1)));
        }

        @Test
        @DisplayName("should throw when endWorkout is null")
        void shouldThrow_whenEndWorkoutIsNull() {
            var workout = validWorkout();

            assertThrows(NullPointerException.class, () -> workout.finish(null));
        }
    }

    // -------------------------------------------------------------------------
    // AddExerciseSeries
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("addExerciseSeries()")
    class AddExerciseSeries {

        @Test
        @DisplayName("should add a new ExerciseSeriesId reference")
        void shouldAddExerciseSeriesId() {
            var workout = validWorkout();
            var esId    = ExerciseSeriesId.generate();

            workout.addExerciseSeries(esId);

            assertTrue(workout.getExerciseSeriesIds().contains(esId));
            assertEquals(1, workout.getExerciseSeriesIds().size());
        }

        @Test
        @DisplayName("should throw when adding duplicate ExerciseSeriesId")
        void shouldThrow_whenAddingDuplicate() {
            var workout = validWorkout();
            var esId    = ExerciseSeriesId.generate();
            workout.addExerciseSeries(esId);

            assertThrows(IllegalArgumentException.class,
                    () -> workout.addExerciseSeries(esId));
        }

        @Test
        @DisplayName("should throw WorkoutAlreadyFinishedException when workout is finished")
        void shouldThrow_whenWorkoutIsFinished() {
            var workout = validWorkout();
            workout.finish(Instant.now());

            assertThrows(WorkoutAlreadyFinishedException.class,
                    () -> workout.addExerciseSeries(ExerciseSeriesId.generate()));
        }

        @Test
        @DisplayName("should throw when exerciseSeriesId is null")
        void shouldThrow_whenExerciseSeriesIdIsNull() {
            var workout = validWorkout();

            assertThrows(NullPointerException.class,
                    () -> workout.addExerciseSeries(null));
        }

        @Test
        @DisplayName("should return unmodifiable list of exerciseSeriesIds")
        void shouldReturnUnmodifiableList() {
            var workout = validWorkout();

            var ids = workout.getExerciseSeriesIds();

            assertThrows(UnsupportedOperationException.class,
                    () -> ids.add(ExerciseSeriesId.generate()));
        }
    }
}


package com.n1b3lung0.gymrat.domain.model;

import com.n1b3lung0.gymrat.domain.event.ExerciseCreated;
import com.n1b3lung0.gymrat.domain.event.ExerciseDeleted;
import com.n1b3lung0.gymrat.domain.event.ExerciseUpdated;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Exercise aggregate")
class ExerciseTest {

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static Exercise validExercise() {
        return Exercise.builder()
                .name("Bench Press")
                .description("Classic chest press on a flat bench")
                .level(Level.INTERMEDIATE)
                .routines(Set.of(Routine.PUSH))
                .primaryMuscle(Muscle.CHEST)
                .secondaryMuscles(Set.of(Muscle.TRICEPS, Muscle.SHOULDERS))
                .build();
    }

    // -------------------------------------------------------------------------
    // Creation
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("should emit ExerciseCreated event when created")
        void shouldEmitExerciseCreatedEvent_whenCreated() {
            var exercise = validExercise();

            var events = exercise.pullDomainEvents();

            assertEquals(1, events.size());
            assertInstanceOf(ExerciseCreated.class, events.getFirst());

            var event = (ExerciseCreated) events.getFirst();
            assertEquals(exercise.getId(), event.exerciseId());
            assertEquals("Bench Press", event.name());
            assertNotNull(event.occurredOn());
        }

        @Test
        @DisplayName("should initialise audit fields with active=true on creation")
        void shouldInitialiseAuditFields_onCreation() {
            var exercise = validExercise();

            var audit = exercise.getAuditFields();

            assertTrue(audit.active());
            assertNotNull(audit.createdAt());
            assertNotNull(audit.createdBy());
            assertNull(audit.updatedAt());
            assertNull(audit.deletedAt());
            assertNull(audit.deletedBy());
        }

        @Test
        @DisplayName("should drain event list after pullDomainEvents()")
        void shouldDrainEventList_afterPull() {
            var exercise = validExercise();
            exercise.pullDomainEvents();

            var secondPull = exercise.pullDomainEvents();

            assertTrue(secondPull.isEmpty());
        }

        @Test
        @DisplayName("should throw when name is blank")
        void shouldThrow_whenNameIsBlank() {
            assertThrows(IllegalArgumentException.class, () ->
                    Exercise.builder()
                            .name("  ")
                            .level(Level.BEGINNER)
                            .routines(Set.of(Routine.PULL))
                            .primaryMuscle(Muscle.BACK)
                            .build()
            );
        }

        @Test
        @DisplayName("should throw when routines is empty")
        void shouldThrow_whenRoutinesIsEmpty() {
            assertThrows(IllegalArgumentException.class, () ->
                    Exercise.builder()
                            .name("Pull-up")
                            .level(Level.ADVANCED)
                            .routines(Set.of())
                            .primaryMuscle(Muscle.BACK)
                            .build()
            );
        }

        @Test
        @DisplayName("should throw when primaryMuscle is null")
        void shouldThrow_whenPrimaryMuscleIsNull() {
            assertThrows(NullPointerException.class, () ->
                    Exercise.builder()
                            .name("Pull-up")
                            .level(Level.ADVANCED)
                            .routines(Set.of(Routine.PULL))
                            .build()
            );
        }

        @Test
        @DisplayName("should generate a non-null UUID id on creation")
        void shouldGenerateId_onCreation() {
            var exercise = validExercise();

            assertNotNull(exercise.getId());
            assertNotNull(exercise.getId().value());
        }
    }

    // -------------------------------------------------------------------------
    // Update
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("should emit ExerciseUpdated event when updated")
        void shouldEmitExerciseUpdatedEvent_whenUpdated() {
            var exercise = validExercise();
            exercise.pullDomainEvents(); // drain creation event

            exercise.update(
                    "Incline Bench Press",
                    "Chest press on an incline bench",
                    Level.ADVANCED,
                    Set.of(Routine.PUSH),
                    Muscle.CHEST,
                    Set.of(Muscle.SHOULDERS),
                    null,
                    null
            );

            var events = exercise.pullDomainEvents();
            assertEquals(1, events.size());
            assertInstanceOf(ExerciseUpdated.class, events.getFirst());

            var event = (ExerciseUpdated) events.getFirst();
            assertEquals(exercise.getId(), event.exerciseId());
        }

        @Test
        @DisplayName("should update name and level")
        void shouldUpdateFields_whenUpdated() {
            var exercise = validExercise();

            exercise.update(
                    "Incline Bench Press",
                    "Updated description",
                    Level.ADVANCED,
                    Set.of(Routine.PUSH, Routine.UPPERBODY),
                    Muscle.CHEST,
                    Set.of(),
                    null,
                    null
            );

            assertEquals("Incline Bench Press", exercise.getName());
            assertEquals(Level.ADVANCED, exercise.getLevel());
            assertNotNull(exercise.getAuditFields().updatedAt());
        }
    }

    // -------------------------------------------------------------------------
    // Delete
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("should emit ExerciseDeleted event when deleted")
        void shouldEmitExerciseDeletedEvent_whenDeleted() {
            var exercise = validExercise();
            exercise.pullDomainEvents(); // drain creation event

            exercise.delete();

            var events = exercise.pullDomainEvents();
            assertEquals(1, events.size());
            assertInstanceOf(ExerciseDeleted.class, events.getFirst());
        }

        @Test
        @DisplayName("should mark exercise as inactive on delete")
        void shouldMarkInactive_onDelete() {
            var exercise = validExercise();

            exercise.delete();

            assertFalse(exercise.getAuditFields().active());
            assertNotNull(exercise.getAuditFields().deletedAt());
            assertTrue(exercise.getAuditFields().isDeleted());
        }
    }

    // -------------------------------------------------------------------------
    // ExerciseSeries reference
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("addExerciseSeries()")
    class AddExerciseSeries {

        @Test
        @DisplayName("should add a new ExerciseSeriesId reference")
        void shouldAddExerciseSeriesId() {
            var exercise = validExercise();
            var esId = ExerciseSeriesId.generate();

            exercise.addExerciseSeries(esId);

            assertTrue(exercise.getExerciseSeriesIds().contains(esId));
        }

        @Test
        @DisplayName("should not add duplicate ExerciseSeriesId")
        void shouldNotAddDuplicate_whenAlreadyPresent() {
            var exercise = validExercise();
            var esId = ExerciseSeriesId.generate();

            exercise.addExerciseSeries(esId);
            exercise.addExerciseSeries(esId);

            assertEquals(1, exercise.getExerciseSeriesIds().size());
        }

        @Test
        @DisplayName("should return unmodifiable list of exerciseSeriesIds")
        void shouldReturnUnmodifiableList() {
            var exercise = validExercise();

            var ids = exercise.getExerciseSeriesIds();

            assertThrows(UnsupportedOperationException.class,
                    () -> ids.add(ExerciseSeriesId.generate()));
        }
    }

    // -------------------------------------------------------------------------
    // Collections immutability
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Collections")
    class Collections_ {

        @Test
        @DisplayName("should return unmodifiable routines set")
        void shouldReturnUnmodifiableRoutines() {
            var exercise = validExercise();

            assertThrows(UnsupportedOperationException.class,
                    () -> exercise.getRoutines().add(Routine.LEG));
        }

        @Test
        @DisplayName("should return unmodifiable secondaryMuscles set")
        void shouldReturnUnmodifiableSecondaryMuscles() {
            var exercise = validExercise();

            assertThrows(UnsupportedOperationException.class,
                    () -> exercise.getSecondaryMuscles().add(Muscle.CORE));
        }
    }
}


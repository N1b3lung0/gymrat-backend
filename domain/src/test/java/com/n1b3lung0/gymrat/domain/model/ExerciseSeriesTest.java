package com.n1b3lung0.gymrat.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ExerciseSeries aggregate")
class ExerciseSeriesTest {

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static ExerciseSeries validExerciseSeries() {
        return ExerciseSeries.create(WorkoutId.generate(), ExerciseId.generate());
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
            var es = validExerciseSeries();

            assertNotNull(es.getId());
            assertNotNull(es.getId().value());
        }

        @Test
        @DisplayName("should store workoutId and exerciseId")
        void shouldStoreParentIds() {
            var workoutId  = WorkoutId.generate();
            var exerciseId = ExerciseId.generate();

            var es = ExerciseSeries.create(workoutId, exerciseId);

            assertEquals(workoutId, es.getWorkoutId());
            assertEquals(exerciseId, es.getExerciseId());
        }

        @Test
        @DisplayName("should start with no series")
        void shouldStartWithNoSeries() {
            var es = validExerciseSeries();

            assertTrue(es.getSeriesIds().isEmpty());
        }

        @Test
        @DisplayName("should initialise audit fields with active=true on creation")
        void shouldInitialiseAuditFields_onCreation() {
            var es    = validExerciseSeries();
            var audit = es.getAuditFields();

            assertTrue(audit.active());
            assertNotNull(audit.createdAt());
            assertNotNull(audit.createdBy());
            assertNull(audit.updatedAt());
            assertNull(audit.deletedAt());
        }

        @Test
        @DisplayName("should throw when workoutId is null")
        void shouldThrow_whenWorkoutIdIsNull() {
            assertThrows(NullPointerException.class,
                    () -> ExerciseSeries.create(null, ExerciseId.generate()));
        }

        @Test
        @DisplayName("should throw when exerciseId is null")
        void shouldThrow_whenExerciseIdIsNull() {
            assertThrows(NullPointerException.class,
                    () -> ExerciseSeries.create(WorkoutId.generate(), null));
        }

        @Test
        @DisplayName("should drain event list after pullDomainEvents()")
        void shouldDrainEventList_afterPull() {
            var es = validExerciseSeries();

            // ExerciseSeries does not emit creation events currently — list must be empty
            var events = es.pullDomainEvents();
            assertTrue(events.isEmpty());

            var secondPull = es.pullDomainEvents();
            assertTrue(secondPull.isEmpty());
        }
    }

    // -------------------------------------------------------------------------
    // AddSeries
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("addSeries()")
    class AddSeries {

        @Test
        @DisplayName("should add a new SeriesId reference")
        void shouldAddSeriesId() {
            var es       = validExerciseSeries();
            var seriesId = SeriesId.generate();

            es.addSeries(seriesId);

            assertTrue(es.getSeriesIds().contains(seriesId));
            assertEquals(1, es.getSeriesIds().size());
        }

        @Test
        @DisplayName("should add multiple distinct SeriesIds")
        void shouldAddMultipleSeriesIds() {
            var es  = validExerciseSeries();
            var id1 = SeriesId.generate();
            var id2 = SeriesId.generate();
            var id3 = SeriesId.generate();

            es.addSeries(id1);
            es.addSeries(id2);
            es.addSeries(id3);

            assertEquals(3, es.getSeriesIds().size());
        }

        @Test
        @DisplayName("should throw when adding duplicate SeriesId")
        void shouldThrow_whenAddingDuplicate() {
            var es       = validExerciseSeries();
            var seriesId = SeriesId.generate();
            es.addSeries(seriesId);

            assertThrows(IllegalArgumentException.class,
                    () -> es.addSeries(seriesId));
        }

        @Test
        @DisplayName("should throw when seriesId is null")
        void shouldThrow_whenSeriesIdIsNull() {
            var es = validExerciseSeries();

            assertThrows(NullPointerException.class, () -> es.addSeries(null));
        }

        @Test
        @DisplayName("should return unmodifiable list of seriesIds")
        void shouldReturnUnmodifiableList() {
            var es = validExerciseSeries();

            var ids = es.getSeriesIds();

            assertThrows(UnsupportedOperationException.class,
                    () -> ids.add(SeriesId.generate()));
        }
    }
}


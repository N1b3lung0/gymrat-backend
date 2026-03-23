package com.n1b3lung0.gymrat.domain.model;

import com.n1b3lung0.gymrat.domain.event.SeriesCreated;
import com.n1b3lung0.gymrat.domain.exception.InvalidRpeIntensityException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Series aggregate")
class SeriesTest {

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static ExerciseSeriesId validExerciseSeriesId() {
        return ExerciseSeriesId.generate();
    }

    private static Series validSeries() {
        return Series.create(
                1,
                10,
                7,
                BigDecimal.valueOf(80),
                RestTime.NINETY,
                validExerciseSeriesId()
        );
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
            var series = validSeries();

            assertNotNull(series.getId());
            assertNotNull(series.getId().value());
        }

        @Test
        @DisplayName("should emit SeriesCreated event when created")
        void shouldEmitSeriesCreatedEvent_whenCreated() {
            var esId   = validExerciseSeriesId();
            var series = Series.create(1, 8, 6, BigDecimal.valueOf(50), RestTime.SIXTY, esId);

            var events = series.pullDomainEvents();

            assertEquals(1, events.size());
            assertInstanceOf(SeriesCreated.class, events.getFirst());

            var event = (SeriesCreated) events.getFirst();
            assertEquals(series.getId(), event.seriesId());
            assertEquals(esId, event.exerciseSeriesId());
            assertEquals(1, event.serialNumber());
            assertNotNull(event.occurredOn());
        }

        @Test
        @DisplayName("should initialise audit fields with active=true on creation")
        void shouldInitialiseAuditFields_onCreation() {
            var series = validSeries();

            var audit = series.getAuditFields();

            assertTrue(audit.active());
            assertNotNull(audit.createdAt());
            assertNotNull(audit.createdBy());
            assertNull(audit.updatedAt());
            assertNull(audit.deletedAt());
        }

        @Test
        @DisplayName("should not have start/end timestamps on creation")
        void shouldHaveNullTimestamps_onCreation() {
            var series = validSeries();

            assertNull(series.getStartSeries());
            assertNull(series.getEndSeries());
        }

        @Test
        @DisplayName("should not have repetitionsDone on creation")
        void shouldHaveNullRepetitionsDone_onCreation() {
            var series = validSeries();

            assertNull(series.getRepetitionsDone());
        }

        @Test
        @DisplayName("should drain event list after pullDomainEvents()")
        void shouldDrainEventList_afterPull() {
            var series = validSeries();
            series.pullDomainEvents();

            var secondPull = series.pullDomainEvents();

            assertTrue(secondPull.isEmpty());
        }

        @Test
        @DisplayName("should throw when repetitionsToDo is zero")
        void shouldThrow_whenRepetitionsToDoIsZero() {
            assertThrows(IllegalArgumentException.class, () ->
                    Series.create(1, 0, 5, null, RestTime.SIXTY, validExerciseSeriesId()));
        }

        @Test
        @DisplayName("should throw when repetitionsToDo is negative")
        void shouldThrow_whenRepetitionsToDoIsNegative() {
            assertThrows(IllegalArgumentException.class, () ->
                    Series.create(1, -1, 5, null, RestTime.SIXTY, validExerciseSeriesId()));
        }

        @Test
        @DisplayName("should throw when serialNumber is zero")
        void shouldThrow_whenSerialNumberIsZero() {
            assertThrows(IllegalArgumentException.class, () ->
                    Series.create(0, 10, 5, null, RestTime.SIXTY, validExerciseSeriesId()));
        }

        @Test
        @DisplayName("should throw when restTime is null")
        void shouldThrow_whenRestTimeIsNull() {
            assertThrows(NullPointerException.class, () ->
                    Series.create(1, 10, 5, null, null, validExerciseSeriesId()));
        }

        @Test
        @DisplayName("should throw when exerciseSeriesId is null")
        void shouldThrow_whenExerciseSeriesIdIsNull() {
            assertThrows(NullPointerException.class, () ->
                    Series.create(1, 10, 5, BigDecimal.TEN, RestTime.SIXTY, null));
        }

        @Test
        @DisplayName("should allow null weight for bodyweight sets")
        void shouldAllowNullWeight_forBodyweightSets() {
            assertDoesNotThrow(() ->
                    Series.create(1, 10, 5, null, RestTime.SIXTY, validExerciseSeriesId()));
        }
    }

    // -------------------------------------------------------------------------
    // RPE Intensity validation
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("RPE intensity validation")
    class RpeIntensity {

        @ParameterizedTest(name = "intensity={0}")
        @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10})
        @DisplayName("should accept all valid RPE values (1–10)")
        void shouldAcceptValidRpe(int intensity) {
            assertDoesNotThrow(() ->
                    Series.create(1, 10, intensity, null, RestTime.SIXTY, validExerciseSeriesId()));
        }

        @ParameterizedTest(name = "intensity={0}")
        @ValueSource(ints = {0, -1, 11, 100})
        @DisplayName("should throw InvalidRpeIntensityException for out-of-range values")
        void shouldThrow_whenIntensityOutOfRange(int intensity) {
            assertThrows(InvalidRpeIntensityException.class, () ->
                    Series.create(1, 10, intensity, null, RestTime.SIXTY, validExerciseSeriesId()));
        }
    }

    // -------------------------------------------------------------------------
    // Finish
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("finish()")
    class Finish {

        @Test
        @DisplayName("should record endSeries and repetitionsDone")
        void shouldRecordFinishData() {
            var series = validSeries();
            var end    = Instant.now();

            series.finish(end, 9);

            assertEquals(end, series.getEndSeries());
            assertEquals(9, series.getRepetitionsDone());
        }

        @Test
        @DisplayName("should throw when endSeries is null")
        void shouldThrow_whenEndSeriesIsNull() {
            var series = validSeries();

            assertThrows(NullPointerException.class, () -> series.finish(null, 10));
        }

        @Test
        @DisplayName("should throw when repetitionsDone is negative")
        void shouldThrow_whenRepetitionsDoneIsNegative() {
            var series = validSeries();

            assertThrows(IllegalArgumentException.class,
                    () -> series.finish(Instant.now(), -1));
        }
    }

    // -------------------------------------------------------------------------
    // Update
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("should update all mutable fields")
        void shouldUpdateFields() {
            var series = validSeries();
            series.pullDomainEvents();

            series.update(12, 11, 8, BigDecimal.valueOf(90), null, null, RestTime.ONE_TWENTY);

            assertEquals(12, series.getRepetitionsToDo());
            assertEquals(11, series.getRepetitionsDone());
            assertEquals(8, series.getIntensity());
            assertEquals(BigDecimal.valueOf(90), series.getWeight());
            assertEquals(RestTime.ONE_TWENTY, series.getRestTime());
        }

        @Test
        @DisplayName("should throw when repetitionsToDo is invalid during update")
        void shouldThrow_whenRepetitionsToDoInvalidOnUpdate() {
            var series = validSeries();

            assertThrows(IllegalArgumentException.class, () ->
                    series.update(0, null, 5, null, null, null, RestTime.SIXTY));
        }

        @Test
        @DisplayName("should throw InvalidRpeIntensityException when intensity is out of range during update")
        void shouldThrow_whenIntensityOutOfRangeOnUpdate() {
            var series = validSeries();

            assertThrows(InvalidRpeIntensityException.class, () ->
                    series.update(10, null, 11, null, null, null, RestTime.SIXTY));
        }
    }
}


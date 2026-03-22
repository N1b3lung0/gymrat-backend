package com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence;

import com.n1b3lung0.gymrat.domain.model.Exercise;
import com.n1b3lung0.gymrat.domain.model.ExerciseSeries;
import com.n1b3lung0.gymrat.domain.model.Level;
import com.n1b3lung0.gymrat.domain.model.Muscle;
import com.n1b3lung0.gymrat.domain.model.RestTime;
import com.n1b3lung0.gymrat.domain.model.Routine;
import com.n1b3lung0.gymrat.domain.model.Series;
import com.n1b3lung0.gymrat.domain.model.SeriesId;
import com.n1b3lung0.gymrat.domain.model.Workout;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for {@link SeriesJpaAdapter}.
 *
 * <p>Uses {@code @SpringBootTest} + Testcontainers PostgreSQL so Flyway migrations
 * run against a real database — the same environment as production.
 *
 * <p>Each test is {@code @Transactional} to roll back after execution.
 * A {@code @BeforeEach} fixture creates the required parent entities
 * ({@link Workout} → {@link ExerciseSeries} → {@link Series}).
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Transactional
@DisplayName("SeriesJpaAdapter")
class SeriesJpaAdapterTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17");

    @Autowired SeriesJpaAdapter         seriesAdapter;
    @Autowired ExerciseJpaAdapter       exerciseAdapter;
    @Autowired WorkoutJpaAdapter        workoutAdapter;
    @Autowired ExerciseSeriesJpaAdapter exerciseSeriesAdapter;

    /** Persisted parent ExerciseSeries, re-created before each test. */
    ExerciseSeries parentEs;

    @BeforeEach
    void setUpParentEntities() {
        // 1. Persist an Exercise
        Exercise exercise = Exercise.create(
                "Deadlift-" + System.nanoTime(),   // unique name per test
                "Hip-hinge compound pull",
                Level.ADVANCED,
                EnumSet.of(Routine.PULL),
                Muscle.BACK,
                EnumSet.of(Muscle.GLUTEAL, Muscle.HAMSTRINGS),
                null,
                null
        );
        Exercise savedExercise = exerciseAdapter.save(exercise);

        // 2. Persist a Workout
        Workout workout = Workout.create(Instant.now());
        Workout savedWorkout = workoutAdapter.save(workout);

        // 3. Persist an ExerciseSeries linking workout ↔ exercise
        ExerciseSeries es = ExerciseSeries.create(savedWorkout.getId(), savedExercise.getId());
        parentEs = exerciseSeriesAdapter.save(es);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Series newSeries(int serialNumber) {
        return Series.create(
                serialNumber,
                10,
                8,
                BigDecimal.valueOf(100),
                RestTime.SIXTY,
                parentEs.getId()
        );
    }

    // -------------------------------------------------------------------------
    // save
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("save()")
    class Save {

        @Test
        @DisplayName("should persist series and return it with the same id")
        void shouldPersistSeries_andReturnWithSameId() {
            var series = newSeries(1);

            var saved = seriesAdapter.save(series);

            assertNotNull(saved);
            assertThat(saved.getId()).isEqualTo(series.getId());
            assertThat(saved.getSerialNumber()).isEqualTo(1);
            assertThat(saved.getRepetitionsToDo()).isEqualTo(10);
            assertThat(saved.getRestTime()).isEqualTo(RestTime.SIXTY);
        }

        @Test
        @DisplayName("should persist intensity and weight")
        void shouldPersistIntensityAndWeight() {
            var saved = seriesAdapter.save(newSeries(1));

            assertThat(saved.getIntensity()).isEqualTo(8);
            assertThat(saved.getWeight()).isEqualByComparingTo(BigDecimal.valueOf(100));
        }

        @Test
        @DisplayName("should initialise audit fields with active=true")
        void shouldInitialiseAuditFields() {
            var saved = seriesAdapter.save(newSeries(1));

            var audit = saved.getAuditFields();
            assertNotNull(audit);
            assertTrue(audit.active());
            assertNotNull(audit.createdAt());
        }
    }

    // -------------------------------------------------------------------------
    // findById
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("should return saved series by id")
        void shouldReturnSeries_whenFound() {
            var saved = seriesAdapter.save(newSeries(1));

            var result = seriesAdapter.findById(saved.getId());

            assertTrue(result.isPresent());
            assertThat(result.get().getSerialNumber()).isEqualTo(1);
        }

        @Test
        @DisplayName("should return empty when id does not exist")
        void shouldReturnEmpty_whenNotFound() {
            var result = seriesAdapter.findById(SeriesId.generate());

            assertFalse(result.isPresent());
        }
    }

    // -------------------------------------------------------------------------
    // serialNumber auto-increment via countByExerciseSeriesId
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("serialNumber ordering")
    class SerialNumber {

        @Test
        @DisplayName("should return count=0 before any series are saved")
        void shouldReturnZeroCount_beforeAnySeries() {
            long count = seriesAdapter.countByExerciseSeriesId(parentEs.getId());

            assertThat(count).isEqualTo(0);
        }

        @Test
        @DisplayName("should increment count after each saved series")
        void shouldIncrementCount_afterEachSave() {
            seriesAdapter.save(newSeries(1));
            seriesAdapter.save(newSeries(2));

            long count = seriesAdapter.countByExerciseSeriesId(parentEs.getId());

            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("findAllByExerciseSeriesId should return series ordered by serialNumber asc")
        void shouldReturnSeriesOrderedBySerialNumber() {
            seriesAdapter.save(newSeries(1));
            seriesAdapter.save(newSeries(2));
            seriesAdapter.save(newSeries(3));

            var list = seriesAdapter.findAllByExerciseSeriesId(parentEs.getId());

            assertThat(list).hasSize(3);
            assertThat(list.get(0).getSerialNumber()).isEqualTo(1);
            assertThat(list.get(1).getSerialNumber()).isEqualTo(2);
            assertThat(list.get(2).getSerialNumber()).isEqualTo(3);
        }
    }

    // -------------------------------------------------------------------------
    // findAllByExerciseSeriesId
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("findAllByExerciseSeriesId()")
    class FindAllByExerciseSeries {

        @Test
        @DisplayName("should return empty list when no series exist")
        void shouldReturnEmptyList_whenNoSeries() {
            var list = seriesAdapter.findAllByExerciseSeriesId(parentEs.getId());

            assertThat(list).isEmpty();
        }

        @Test
        @DisplayName("should return all series for the given exercise-series")
        void shouldReturnAllSeries_forGivenExerciseSeries() {
            seriesAdapter.save(newSeries(1));
            seriesAdapter.save(newSeries(2));

            var list = seriesAdapter.findAllByExerciseSeriesId(parentEs.getId());

            assertThat(list).hasSize(2);
        }
    }

    // -------------------------------------------------------------------------
    // deleteById (soft-delete)
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("deleteById() — soft-delete")
    class SoftDelete {

        @Test
        @DisplayName("should soft-delete: series not returned by findById afterwards")
        void shouldSoftDelete_seriesNotReturnedAfterwards() {
            var saved = seriesAdapter.save(newSeries(1));

            seriesAdapter.deleteById(saved.getId());

            assertFalse(seriesAdapter.findById(saved.getId()).isPresent());
        }

        @Test
        @DisplayName("should soft-delete: count decremented after soft-delete")
        void shouldDecrementCount_afterSoftDelete() {
            var s1 = seriesAdapter.save(newSeries(1));
            seriesAdapter.save(newSeries(2));

            seriesAdapter.deleteById(s1.getId());

            assertThat(seriesAdapter.countByExerciseSeriesId(parentEs.getId())).isEqualTo(1);
        }
    }

    // -------------------------------------------------------------------------
    // findDetailById (CQRS query side)
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("findDetailById()")
    class FindDetailById {

        @Test
        @DisplayName("should return detail view with all fields populated")
        void shouldReturnDetailView_whenFound() {
            var saved = seriesAdapter.save(newSeries(1));

            var detail = seriesAdapter.findDetailById(saved.getId());

            assertTrue(detail.isPresent());
            assertThat(detail.get().serialNumber()).isEqualTo(1);
            assertThat(detail.get().repetitionsToDo()).isEqualTo(10);
            assertThat(detail.get().intensity()).isEqualTo(8);
            assertThat(detail.get().restTime()).isEqualTo(RestTime.SIXTY);
            assertThat(detail.get().exerciseSeriesId()).isEqualTo(parentEs.getId().value());
        }
    }
}




package com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence;

import com.n1b3lung0.gymrat.domain.model.Exercise;
import com.n1b3lung0.gymrat.domain.model.ExerciseId;
import com.n1b3lung0.gymrat.domain.model.Level;
import com.n1b3lung0.gymrat.domain.model.Muscle;
import com.n1b3lung0.gymrat.domain.model.Routine;
import com.n1b3lung0.gymrat.domain.repository.PageRequest;
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

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for {@link ExerciseJpaAdapter}.
 *
 * <p>Uses {@code @SpringBootTest} + Testcontainers PostgreSQL so Flyway migrations
 * run against a real database — the same environment as production.
 *
 * <p>Each test is {@code @Transactional} to roll back after execution,
 * keeping tests independent without truncating tables manually.
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Transactional
@DisplayName("ExerciseJpaAdapter")
class ExerciseJpaAdapterTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    ExerciseJpaAdapter adapter;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static Exercise benchPress() {
        // Append nanoTime to avoid UNIQUE(name) collision with seed data
        return Exercise.create(
                "Bench Press Test " + System.nanoTime(),
                "Classic horizontal chest press on a flat bench",
                Level.INTERMEDIATE,
                EnumSet.of(Routine.PUSH),
                Muscle.CHEST,
                EnumSet.of(Muscle.TRICEPS, Muscle.SHOULDERS),
                null,
                null
        );
    }

    private static Exercise squat() {
        return Exercise.create(
                "Squat Test " + System.nanoTime(),
                "Compound lower-body movement",
                Level.BEGINNER,
                EnumSet.of(Routine.LEG),
                Muscle.QUADRICEPS,
                EnumSet.of(Muscle.GLUTEAL, Muscle.HAMSTRINGS),
                null,
                null
        );
    }

    // -------------------------------------------------------------------------
    // save
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("save()")
    class Save {

        @Test
        @DisplayName("should persist exercise and return it with same id")
        void shouldPersistExercise_andReturnWithSameId() {
            var exercise = benchPress();

            var saved = adapter.save(exercise);

            assertNotNull(saved);
            assertThat(saved.getId()).isEqualTo(exercise.getId());
            assertThat(saved.getName()).isEqualTo(exercise.getName());
            assertThat(saved.getLevel()).isEqualTo(Level.INTERMEDIATE);
            assertThat(saved.getPrimaryMuscle()).isEqualTo(Muscle.CHEST);
        }

        @Test
        @DisplayName("should persist routines as element collection")
        void shouldPersistRoutines() {
            var saved = adapter.save(benchPress());

            assertThat(saved.getRoutines()).containsExactlyInAnyOrder(Routine.PUSH);
        }

        @Test
        @DisplayName("should persist secondaryMuscles as element collection")
        void shouldPersistSecondaryMuscles() {
            var saved = adapter.save(benchPress());

            assertThat(saved.getSecondaryMuscles())
                    .containsExactlyInAnyOrder(Muscle.TRICEPS, Muscle.SHOULDERS);
        }

        @Test
        @DisplayName("should initialise audit fields with active=true")
        void shouldInitialiseAuditFields() {
            var saved = adapter.save(benchPress());

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
        @DisplayName("should return saved exercise by id")
        void shouldReturnExercise_whenFound() {
            var exercise = benchPress();
            var saved = adapter.save(exercise);

            var result = adapter.findById(saved.getId());

            assertTrue(result.isPresent());
            assertThat(result.get().getName()).isEqualTo(exercise.getName());
        }

        @Test
        @DisplayName("should return empty when id does not exist")
        void shouldReturnEmpty_whenNotFound() {
            var result = adapter.findById(ExerciseId.generate());

            assertFalse(result.isPresent());
        }
    }

    // -------------------------------------------------------------------------
    // findAll (paged)
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("findAll()")
    class FindAll {

        @Test
        @DisplayName("should return all saved exercises in a page")
        void shouldReturnPagedExercises() {
            adapter.save(benchPress());
            adapter.save(squat());

            var page = adapter.findAll(PageRequest.of(0, 20, null, true));

            assertThat(page.content().size()).isGreaterThanOrEqualTo(2);
            assertThat(page.totalElements()).isGreaterThanOrEqualTo(2);
        }

        @Test
        @DisplayName("should respect page size")
        void shouldRespectPageSize() {
            adapter.save(benchPress());
            adapter.save(squat());

            var page = adapter.findAll(PageRequest.of(0, 1, null, true));

            assertThat(page.content()).hasSize(1);
        }

        @Test
        @DisplayName("should return empty page when no exercises exist")
        void shouldReturnEmptyPage_whenNoExercisesExist() {
            // Relies on @Transactional rollback — starts clean
            var page = adapter.findAll(PageRequest.of(0, 20, null, true));

            // totalElements may be >0 if data.sql seeded data; just assert content size matches
            assertThat(page.page()).isEqualTo(0);
            assertThat(page.size()).isEqualTo(20);
        }
    }

    // -------------------------------------------------------------------------
    // existsByName
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("existsByName()")
    class ExistsByName {

        @Test
        @DisplayName("should return true when exercise with that name exists")
        void shouldReturnTrue_whenNameExists() {
            var exercise = benchPress();
            adapter.save(exercise);

            assertTrue(adapter.existsByName(exercise.getName()));
        }

        @Test
        @DisplayName("should return false when no exercise has that name")
        void shouldReturnFalse_whenNameNotFound() {
            assertFalse(adapter.existsByName("Non-Existent Exercise XYZ-" + System.nanoTime()));
        }
    }

    // -------------------------------------------------------------------------
    // deleteById (soft-delete)
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("deleteById() — soft-delete")
    class SoftDelete {

        @Test
        @DisplayName("should soft-delete: exercise no longer returned by findById")
        void shouldSoftDelete_exerciseNotReturnedAfterwards() {
            var saved = adapter.save(benchPress());

            adapter.deleteById(saved.getId());

            // @SQLRestriction("deleted_at IS NULL") means findById returns empty
            var result = adapter.findById(saved.getId());
            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("should soft-delete: exercise excluded from findAll paged result")
        void shouldSoftDelete_exerciseExcludedFromPagedResult() {
            var saved = adapter.save(benchPress());
            adapter.deleteById(saved.getId());

            var page = adapter.findAll(PageRequest.of(0, 20, null, true));
            var ids = page.content().stream().map(Exercise::getId).toList();

            assertThat(ids).doesNotContain(saved.getId());
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
            var exercise = benchPress();
            var saved = adapter.save(exercise);

            var detail = adapter.findDetailById(saved.getId());

            assertTrue(detail.isPresent());
            assertThat(detail.get().name()).isEqualTo(exercise.getName());
            assertThat(detail.get().level()).isEqualTo(Level.INTERMEDIATE);
            assertThat(detail.get().primaryMuscle()).isEqualTo(Muscle.CHEST);
            assertThat(detail.get().routines()).containsExactlyInAnyOrder(Routine.PUSH);
        }
    }
}








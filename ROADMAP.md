# GymRat — Implementation Roadmap

Incremental, testable step-by-step guide to build the full CRUD API for `Exercise`, `ExerciseSeries`, `Series` and `Workout` following Hexagonal Architecture, DDD, CQRS and the conventions defined in `CLAUDE.md`.

Each step produces a **compilable, runnable, testable piece** of code. No step depends on a future step to compile.

---

## Domain Model Overview

```
Workout
  └── has many ExerciseSeries

ExerciseSeries          ← join entity between Workout, Exercise and Series
  ├── belongs to Workout
  ├── belongs to Exercise
  └── has many Series

Exercise
  ├── name, description, level (enum)
  ├── routines (Set<Routine> enum)
  ├── primaryMuscle (Muscle enum)
  ├── secondaryMuscles (Set<Muscle>)
  ├── image (embedded Media)
  ├── video (embedded Media)
  └── has many ExerciseSeries

Series
  ├── serialNumber (auto-generated order within exercise)
  ├── repetitionsToDo, repetitionsDone
  ├── intensity (RPE 1-10)
  ├── weight
  ├── startSeries, endSeries
  ├── restTime (RestTime enum)
  └── belongs to ExerciseSeries
```

---

## Index

| Phase | Steps | Description |
|-------|-------|-------------|
| 0 | 1–5   | Project restructure to multi-module Gradle |
| 1 | 6–15  | Domain layer: enums, value objects, domain models |
| 2 | 16–22 | Domain layer: exceptions, repository ports, domain events |
| 3 | 23–32 | Application layer: commands, queries, use case ports, handlers |
| 4 | 33–47 | Infrastructure layer: JPA entities, Flyway migrations, JPA adapters |
| 5 | 48–58 | Infrastructure layer: REST controllers, DTOs, mappers, config beans |
| 6 | 59–62 | Cross-cutting: exception handler, OpenAPI, observability, data seeds |
| 7 | 63–70 | Testing: unit tests, slice tests, architecture tests |
| 8 | 71–80 | End-to-end: full integration tests, security, performance, deployment |

---

## Phase 0 — Multi-Module Gradle Restructure

### Step 1 — Migrate build files from Groovy DSL to Kotlin DSL

**Goal:** rename `build.gradle` → `build.gradle.kts` and `settings.gradle` → `settings.gradle.kts`, migrate syntax.  
**Files:**
- `settings.gradle.kts` (root)
- `build.gradle.kts` (root)

**Verify:** `./gradlew help` compiles without errors.

---

### Step 2 — Create `gradle/libs.versions.toml` (Version Catalog)

**Goal:** declare all dependency versions and library aliases centrally.  
**Files:**
- `gradle/libs.versions.toml`

**Verify:** `./gradlew dependencies` resolves without errors.

---

### Step 3 — Create `:domain` module

**Goal:** bare module with `build.gradle.kts` (no Spring dependencies, pure Java).  
**Files:**
- `domain/build.gradle.kts`
- `domain/src/main/java/com/n1b3lung0/gymrat/.gitkeep`
- Register `:domain` in `settings.gradle.kts`

**Verify:** `./gradlew :domain:compileJava` succeeds.

---

### Step 4 — Create `:application` module

**Goal:** bare module depending only on `:domain`.  
**Files:**
- `application/build.gradle.kts`
- `application/src/main/java/com/n1b3lung0/gymrat/.gitkeep`
- Register `:application` in `settings.gradle.kts`

**Verify:** `./gradlew :application:compileJava` succeeds.

---

### Step 5 — Create `:infrastructure` module and move existing Spring Boot wiring

**Goal:** `:infrastructure` depends on `:application` + `:domain`. Move `GymratApplication.java`, `application.yaml`, `compose.yaml` into the infra module. Root `build.gradle.kts` becomes a conventions-only file.  
**Files:**
- `infrastructure/build.gradle.kts`
- `infrastructure/src/main/java/com/n1b3lung0/gymrat/GymratApplication.java`
- `infrastructure/src/main/resources/application.yaml`
- Register `:infrastructure` in `settings.gradle.kts`

**Verify:** `./gradlew :infrastructure:bootRun` starts the application.

---

## Phase 1 — Domain Layer: Enums and Value Objects

### Step 6 — Create `Routine` enum

**Module:** `:domain`  
**Package:** `com.n1b3lung0.gymrat.domain.model`  
**File:** `Routine.java`  
**Values:** `PUSH, PULL, LEG, FULLBODY, UPPERBODY, ARMS, SHOULDERS`

**Verify:** `./gradlew :domain:compileJava` succeeds.

---

### Step 7 — Create `Muscle` enum

**Module:** `:domain`  
**Package:** `com.n1b3lung0.gymrat.domain.model`  
**File:** `Muscle.java`  
**Values:** `CHEST, BACK, SHOULDERS, BICEPS, TRICEPS, QUADRICEPS, HAMSTRINGS, GLUTEAL, CORE, LUMBAR, CALFS, ABDUCTORS, ADDUCTORS, FOREARMS, TRAPEZE`

**Verify:** `./gradlew :domain:compileJava` succeeds.

---

### Step 8 — Create `Level` enum

**Module:** `:domain`  
**Package:** `com.n1b3lung0.gymrat.domain.model`  
**File:** `Level.java`  
**Values:** `BEGINNER, INTERMEDIATE, ADVANCED`

**Verify:** `./gradlew :domain:compileJava` succeeds.

---

### Step 9 — Create `RestTime` enum

**Module:** `:domain`  
**Package:** `com.n1b3lung0.gymrat.domain.model`  
**File:** `RestTime.java`  
**Values:** `THIRTY(30), SIXTY(60), NINETY(90), ONE_TWENTY(120), ONE_EIGHTY(180), TWO_FORTY(240), THREE_HUNDRED(300)` — each with an `int seconds` field.

**Verify:** `./gradlew :domain:compileJava` succeeds.

---

### Step 10 — Create Value Object `ExerciseId`

**Module:** `:domain`  
**Package:** `com.n1b3lung0.gymrat.domain.model`  
**File:** `ExerciseId.java` — `record ExerciseId(UUID value)` with `generate()` factory and `of(UUID)` factory. Validation in compact constructor.

**Verify:** `./gradlew :domain:compileJava` succeeds.

---

### Step 11 — Create Value Objects `SeriesId`, `ExerciseSeriesId`, `WorkoutId`

**Module:** `:domain`  
**Package:** `com.n1b3lung0.gymrat.domain.model`  
**Files:** `SeriesId.java`, `ExerciseSeriesId.java`, `WorkoutId.java` — same pattern as `ExerciseId`.

**Verify:** `./gradlew :domain:compileJava` succeeds.

---

### Step 12 — Create `Media` value object (replaces image/video embedded class)

**Module:** `:domain`  
**Package:** `com.n1b3lung0.gymrat.domain.model`  
**File:** `Media.java` — `record Media(String name, String description, String url)`. Validation: `url` not blank.

> **Design note:** `Media` is modeled as a separate JPA table (`media`) to avoid duplication, referenced by `Exercise` via foreign keys (`image_id`, `video_id`). In the domain it remains a simple Value Object record; ownership of deduplication lives in the persistence adapter.

**Verify:** `./gradlew :domain:compileJava` succeeds.

---

### Step 13 — Create `AuditFields` value object

**Module:** `:domain`  
**Package:** `com.n1b3lung0.gymrat.domain.model`  
**File:** `AuditFields.java`

```java
public record AuditFields(
    Instant createdAt,
    String createdBy,
    Instant updatedAt,
    String updatedBy,
    boolean active,
    Instant deletedAt,   // null = active
    String deletedBy     // null = active
) {}
```

> **Design note:** `AuditFields` lives in the domain as a Value Object because the business does need to reason about it (soft-delete, active/inactive status). The JPA entity will embed these fields using `@Embedded` + `@AttributeOverrides`.

**Verify:** `./gradlew :domain:compileJava` succeeds.

---

## Phase 1 (cont.) — Domain Aggregates

### Step 14 — Create `Exercise` aggregate root

**Module:** `:domain`  
**Package:** `com.n1b3lung0.gymrat.domain.model`  
**File:** `Exercise.java`

Fields:
- `ExerciseId id`
- `String name`
- `String description`
- `Level level`
- `Set<Routine> routines`
- `Muscle primaryMuscle`
- `Set<Muscle> secondaryMuscles`
- `Media image` (nullable)
- `Media video` (nullable)
- `List<ExerciseSeriesId> exerciseSeriesIds` ← reference by ID only (DDD rule)
- `AuditFields auditFields`

Factory method: `Exercise.create(name, description, level, routines, primaryMuscle, secondaryMuscles, image, video)`  
Business method: `exercise.update(name, description, level, routines, primaryMuscle, secondaryMuscles, image, video)`  
Package-private reconstitution constructor.

**Verify:** `./gradlew :domain:compileJava` succeeds.

---

### Step 15 — Create `Series`, `ExerciseSeries` and `Workout` aggregates

**Module:** `:domain`  
**Package:** `com.n1b3lung0.gymrat.domain.model`

**`Series.java`** fields: `SeriesId id`, `int serialNumber`, `int repetitionsToDo`, `Integer repetitionsDone`, `int intensity` (1-10 RPE), `BigDecimal weight`, `Instant startSeries`, `Instant endSeries`, `RestTime restTime`, `ExerciseSeriesId exerciseSeriesId`, `AuditFields auditFields`  
Factory: `Series.create(repetitionsToDo, intensity, weight, restTime, exerciseSeriesId)`

**`ExerciseSeries.java`** fields: `ExerciseSeriesId id`, `WorkoutId workoutId`, `ExerciseId exerciseId`, `List<SeriesId> seriesIds`, `AuditFields auditFields`  
Factory: `ExerciseSeries.create(workoutId, exerciseId)`  
Business method: `exerciseSeries.addSeries(seriesId)`

**`Workout.java`** fields: `WorkoutId id`, `Instant startWorkout`, `Instant endWorkout`, `List<ExerciseSeriesId> exerciseSeriesIds`, `AuditFields auditFields`  
Factory: `Workout.create(startWorkout)`  
Business method: `workout.finish(endWorkout)`, `workout.addExerciseSeries(exerciseSeriesId)`

**Verify:** `./gradlew :domain:compileJava` succeeds.

---

## Phase 2 — Domain Layer: Exceptions, Ports, Events

### Step 16 — Create domain exception hierarchy

**Module:** `:domain`  
**Package:** `com.n1b3lung0.gymrat.domain.exception`  
**Files:**
- `DomainException.java` (abstract, extends RuntimeException)
- `NotFoundException.java` (abstract, extends DomainException)
- `BusinessRuleViolationException.java` (abstract, extends DomainException)
- `ConflictException.java` (abstract, extends DomainException)

**Verify:** `./gradlew :domain:compileJava` succeeds.

---

### Step 17 — Create concrete domain exceptions for Exercise

**Module:** `:domain`  
**Package:** `com.n1b3lung0.gymrat.domain.exception`  
**Files:**
- `ExerciseNotFoundException.java` — extends `NotFoundException`
- `DuplicateExerciseNameException.java` — extends `ConflictException`
- `InvalidRpeIntensityException.java` — extends `BusinessRuleViolationException` (intensity 1-10)

**Verify:** `./gradlew :domain:compileJava` succeeds.

---

### Step 18 — Create concrete domain exceptions for Series, ExerciseSeries, Workout

**Module:** `:domain`  
**Package:** `com.n1b3lung0.gymrat.domain.exception`  
**Files:**
- `SeriesNotFoundException.java`
- `ExerciseSeriesNotFoundException.java`
- `WorkoutNotFoundException.java`
- `WorkoutAlreadyFinishedException.java` — extends `BusinessRuleViolationException`

**Verify:** `./gradlew :domain:compileJava` succeeds.

---

### Step 19 — Create `ExerciseRepositoryPort` and `MediaRepositoryPort`

**Module:** `:domain`  
**Package:** `com.n1b3lung0.gymrat.domain.repository`  
**Files:**
- `ExerciseRepositoryPort.java` — `save`, `findById`, `findAll(Pageable)`, `deleteById`, `existsByName`
- `MediaRepositoryPort.java` — `save`, `findById`, `findByUrl`

**Verify:** `./gradlew :domain:compileJava` succeeds.

---

### Step 20 — Create `SeriesRepositoryPort`, `ExerciseSeriesRepositoryPort`, `WorkoutRepositoryPort`

**Module:** `:domain`  
**Package:** `com.n1b3lung0.gymrat.domain.repository`  
**Files:**
- `SeriesRepositoryPort.java` — `save`, `findById`, `findAllByExerciseSeriesId`, `deleteById`, `countByExerciseSeriesId` (for auto serialNumber)
- `ExerciseSeriesRepositoryPort.java` — `save`, `findById`, `findAllByWorkoutId`, `deleteById`
- `WorkoutRepositoryPort.java` — `save`, `findById`, `findAll(Pageable)`, `deleteById`

**Verify:** `./gradlew :domain:compileJava` succeeds.

---

### Step 21 — Create domain events (sealed interfaces + records)

**Module:** `:domain`  
**Package:** `com.n1b3lung0.gymrat.domain.event`  
**Files:**

```
ExerciseEvent.java       sealed interface
  ExerciseCreated.java   record (exerciseId, name, occurredOn)
  ExerciseUpdated.java   record (exerciseId, occurredOn)
  ExerciseDeleted.java   record (exerciseId, occurredOn)

WorkoutEvent.java        sealed interface
  WorkoutStarted.java    record (workoutId, startWorkout, occurredOn)
  WorkoutFinished.java   record (workoutId, endWorkout, occurredOn)

SeriesEvent.java         sealed interface
  SeriesCreated.java     record (seriesId, exerciseSeriesId, serialNumber, occurredOn)
```

**Verify:** `./gradlew :domain:compileJava` succeeds.

---

### Step 22 — Add domain event accumulation to aggregates

**Module:** `:domain`  
**Goal:** add `List<? extends Object> domainEvents` + `pullDomainEvents()` to `Exercise`, `Workout` and `Series` following the pattern in CLAUDE.md. Emit events from factory methods and business methods.

**Verify:** `./gradlew :domain:test` — write first unit test: `ExerciseTest.shouldEmitExerciseCreatedEvent_whenCreated()`.

---

## Phase 3 — Application Layer: CQRS Use Cases

### Step 23 — Create application module structure and `DomainEventPublisherPort`

**Module:** `:application`  
**Package:** `com.n1b3lung0.gymrat.application.port.output`  
**File:** `DomainEventPublisherPort.java`

```java
public interface DomainEventPublisherPort {
    void publish(Object event);
}
```

**Verify:** `./gradlew :application:compileJava` succeeds.

---

### Step 24 — Create Exercise Query Port and View DTOs

**Module:** `:application`  
**Package:** `com.n1b3lung0.gymrat.application.port.output`  
**Files:**
- `ExerciseQueryPort.java` — `findDetailById(ExerciseId)`, `findAll(Pageable)` returning `Page<ExerciseSummaryView>`
- `com.n1b3lung0.gymrat.application.dto.ExerciseDetailView.java` (record)
- `com.n1b3lung0.gymrat.application.dto.ExerciseSummaryView.java` (record)
- `com.n1b3lung0.gymrat.application.dto.MediaView.java` (record)

**Verify:** `./gradlew :application:compileJava` succeeds.

---

### Step 25 — Create Exercise Use Case input ports

**Module:** `:application`  
**Package:** `com.n1b3lung0.gymrat.application.port.input`  
**Files:**
- `command/CreateExerciseUseCase.java` — `ExerciseId execute(CreateExerciseCommand)`
- `command/UpdateExerciseUseCase.java` — `void execute(UpdateExerciseCommand)`
- `command/DeleteExerciseUseCase.java` — `void execute(DeleteExerciseCommand)`
- `query/GetExerciseByIdUseCase.java` — `ExerciseDetailView execute(GetExerciseByIdQuery)`
- `query/ListExercisesUseCase.java` — `Page<ExerciseSummaryView> execute(ListExercisesQuery)`

**Verify:** `./gradlew :application:compileJava` succeeds.

---

### Step 26 — Create Exercise Commands and Queries (records)

**Module:** `:application`  
**Package:** `com.n1b3lung0.gymrat.application.dto`  
**Files:**
- `CreateExerciseCommand.java` (record) — name, description, level, routines, primaryMuscle, secondaryMuscles, imageUrl/name/desc, videoUrl/name/desc, idempotencyKey
- `UpdateExerciseCommand.java` (record) — id + same fields
- `DeleteExerciseCommand.java` (record) — id
- `GetExerciseByIdQuery.java` (record) — id
- `ListExercisesQuery.java` (record) — Pageable

**Verify:** `./gradlew :application:compileJava` succeeds.

---

### Step 27 — Create `CreateExerciseHandler`

**Module:** `:application`  
**Package:** `com.n1b3lung0.gymrat.application.command`  
**File:** `CreateExerciseHandler.java`

Logic:
1. Check idempotency (skip for now, hook later).
2. Check `existsByName` → throw `DuplicateExerciseNameException` if exists.
3. Resolve/save `Media` objects via `MediaRepositoryPort`.
4. `Exercise.create(...)`.
5. `exerciseRepository.save(exercise)`.
6. `exercise.pullDomainEvents().forEach(publisher::publish)`.
7. Increment `exercises.created.total` counter.
8. Return `ExerciseId`.

**Unit test:** `CreateExerciseHandlerTest` — mock all ports, verify event published, counter incremented.

**Verify:** `./gradlew :application:test` — tests pass.

---

### Step 28 — Create `UpdateExerciseHandler` and `DeleteExerciseHandler`

**Module:** `:application`  
**Package:** `com.n1b3lung0.gymrat.application.command`

`UpdateExerciseHandler`: load → call `exercise.update(...)` → save → publish events.  
`DeleteExerciseHandler`: load → `deleteById` → publish `ExerciseDeleted` event (manual emit).

**Unit tests** for both handlers.

**Verify:** `./gradlew :application:test` — tests pass.

---

### Step 29 — Create `GetExerciseByIdHandler` and `ListExercisesHandler`

**Module:** `:application`  
**Package:** `com.n1b3lung0.gymrat.application.query`

`GetExerciseByIdHandler`: delegates to `ExerciseQueryPort.findDetailById(id)` → throws `ExerciseNotFoundException` if empty.  
`ListExercisesHandler`: delegates to `ExerciseQueryPort.findAll(pageable)`.

**Unit tests** for both handlers.

**Verify:** `./gradlew :application:test` — tests pass.

---

### Step 30 — Create Workout + ExerciseSeries + Series Use Case ports, Commands, Queries and Handlers

**Module:** `:application`  
**Packages:** `port.input.command`, `port.input.query`, `dto`, `command`, `query`

**Workout:**
- `CreateWorkoutUseCase`, `FinishWorkoutUseCase`, `DeleteWorkoutUseCase`
- `GetWorkoutByIdUseCase`, `ListWorkoutsUseCase`
- `CreateWorkoutCommand`, `FinishWorkoutCommand`, `DeleteWorkoutCommand`, `GetWorkoutByIdQuery`, `ListWorkoutsQuery`
- `WorkoutDetailView`, `WorkoutSummaryView`
- `WorkoutQueryPort`
- Handlers: `CreateWorkoutHandler`, `FinishWorkoutHandler`, `DeleteWorkoutHandler`, `GetWorkoutByIdHandler`, `ListWorkoutsHandler`

**Verify:** `./gradlew :application:test` — unit tests pass.

---

### Step 31 — Create ExerciseSeries Use Case ports, Commands, Queries and Handlers

**Module:** `:application`

- `AddExerciseToWorkoutUseCase` — creates ExerciseSeries linking Workout + Exercise
- `RemoveExerciseFromWorkoutUseCase`
- `GetExerciseSeriesByIdUseCase`, `ListExerciseSeriesByWorkoutUseCase`
- Commands, Queries, Views, QueryPort, Handlers

**Verify:** `./gradlew :application:test` — unit tests pass.

---

### Step 32 — Create Series Use Case ports, Commands, Queries and Handlers

**Module:** `:application`

- `RecordSeriesUseCase` — creates a Series within an ExerciseSeries (auto-increments serialNumber)
- `UpdateSeriesUseCase`, `DeleteSeriesUseCase`
- `GetSeriesByIdUseCase`, `ListSeriesByExerciseSeriesUseCase`
- Commands, Queries, Views, QueryPort, Handlers

**Verify:** `./gradlew :application:test` — unit tests pass.

---

## Phase 4 — Infrastructure Layer: Persistence

### Step 33 — Configure Flyway and PostgreSQL in `:infrastructure`

**Module:** `:infrastructure`  
**Files:**
- `infrastructure/src/main/resources/application.yaml` — configure datasource, JPA `ddl-auto: validate`, Flyway enabled
- `infrastructure/src/main/resources/db/migration/` directory

**Verify:** `./gradlew :infrastructure:bootRun` starts, Flyway runs (no migrations yet = OK).

---

### Step 34 — Create `media` table migration (V1)

**File:** `V1__create_media_table.sql`

```sql
CREATE TABLE media (
    id          UUID PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    url         VARCHAR(2048) NOT NULL UNIQUE,
    created_at  TIMESTAMPTZ NOT NULL,
    created_by  VARCHAR(255) NOT NULL,
    updated_at  TIMESTAMPTZ NOT NULL,
    updated_by  VARCHAR(255) NOT NULL,
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at  TIMESTAMPTZ,
    deleted_by  VARCHAR(255)
);
CREATE INDEX ON media (url) WHERE deleted_at IS NULL;
```

**Verify:** `./gradlew :infrastructure:bootRun` — Flyway applies V1 successfully.

---

### Step 35 — Create `exercises` table migration (V2)

**File:** `V2__create_exercises_table.sql`

```sql
CREATE TABLE exercises (
    id                  UUID PRIMARY KEY,
    name                VARCHAR(255) NOT NULL UNIQUE,
    description         TEXT,
    level               VARCHAR(50) NOT NULL,
    primary_muscle      VARCHAR(50) NOT NULL,
    image_id            UUID REFERENCES media(id),
    video_id            UUID REFERENCES media(id),
    created_at          TIMESTAMPTZ NOT NULL,
    created_by          VARCHAR(255) NOT NULL,
    updated_at          TIMESTAMPTZ NOT NULL,
    updated_by          VARCHAR(255) NOT NULL,
    active              BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at          TIMESTAMPTZ,
    deleted_by          VARCHAR(255)
);
CREATE INDEX ON exercises (name) WHERE deleted_at IS NULL;

CREATE TABLE exercise_routines (
    exercise_id UUID NOT NULL REFERENCES exercises(id),
    routine     VARCHAR(50) NOT NULL,
    PRIMARY KEY (exercise_id, routine)
);

CREATE TABLE exercise_secondary_muscles (
    exercise_id      UUID NOT NULL REFERENCES exercises(id),
    secondary_muscle VARCHAR(50) NOT NULL,
    PRIMARY KEY (exercise_id, secondary_muscle)
);
```

**Verify:** `./gradlew :infrastructure:bootRun` — Flyway applies V2.

---

### Step 36 — Create `workouts` table migration (V3)

**File:** `V3__create_workouts_table.sql`

```sql
CREATE TABLE workouts (
    id            UUID PRIMARY KEY,
    start_workout TIMESTAMPTZ NOT NULL,
    end_workout   TIMESTAMPTZ,
    created_at    TIMESTAMPTZ NOT NULL,
    created_by    VARCHAR(255) NOT NULL,
    updated_at    TIMESTAMPTZ NOT NULL,
    updated_by    VARCHAR(255) NOT NULL,
    active        BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at    TIMESTAMPTZ,
    deleted_by    VARCHAR(255)
);
```

**Verify:** `./gradlew :infrastructure:bootRun` — Flyway applies V3.

---

### Step 37 — Create `exercise_series` and `series` table migrations (V4, V5)

**File:** `V4__create_exercise_series_table.sql`

```sql
CREATE TABLE exercise_series (
    id          UUID PRIMARY KEY,
    workout_id  UUID NOT NULL REFERENCES workouts(id),
    exercise_id UUID NOT NULL REFERENCES exercises(id),
    created_at  TIMESTAMPTZ NOT NULL,
    created_by  VARCHAR(255) NOT NULL,
    updated_at  TIMESTAMPTZ NOT NULL,
    updated_by  VARCHAR(255) NOT NULL,
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at  TIMESTAMPTZ,
    deleted_by  VARCHAR(255),
    UNIQUE (workout_id, exercise_id)   -- an exercise appears once per workout
);
```

**File:** `V5__create_series_table.sql`

```sql
CREATE TABLE series (
    id                  UUID PRIMARY KEY,
    serial_number       INT NOT NULL,
    repetitions_to_do   INT NOT NULL,
    repetitions_done    INT,
    intensity           INT NOT NULL CHECK (intensity BETWEEN 1 AND 10),
    weight              NUMERIC(6,2),
    start_series        TIMESTAMPTZ,
    end_series          TIMESTAMPTZ,
    rest_time           INT NOT NULL,
    exercise_series_id  UUID NOT NULL REFERENCES exercise_series(id),
    created_at          TIMESTAMPTZ NOT NULL,
    created_by          VARCHAR(255) NOT NULL,
    updated_at          TIMESTAMPTZ NOT NULL,
    updated_by          VARCHAR(255) NOT NULL,
    active              BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at          TIMESTAMPTZ,
    deleted_by          VARCHAR(255),
    UNIQUE (exercise_series_id, serial_number)
);
```

**Verify:** `./gradlew :infrastructure:bootRun` — Flyway applies V4 and V5.

---

### Step 38 — Create `AuditEmbeddable` JPA class

**Module:** `:infrastructure`  
**Package:** `com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence.entity`  
**File:** `AuditEmbeddable.java` — `@Embeddable` with JPA Auditing annotations (`@CreatedDate`, `@LastModifiedDate`, `@CreatedBy`, `@LastModifiedBy`) plus `active`, `deletedAt`, `deletedBy`.

**Verify:** `./gradlew :infrastructure:compileJava` succeeds.

---

### Step 39 — Create `MediaEntity` JPA entity

**Module:** `:infrastructure`  
**Package:** `...persistence.entity`  
**File:** `MediaEntity.java` — `@Entity @Table(name="media")` with `@SQLRestriction("deleted_at IS NULL")` and `@SQLDelete(...)`.

**Verify:** `./gradlew :infrastructure:compileJava` succeeds.

---

### Step 40 — Create `ExerciseEntity` JPA entity

**Module:** `:infrastructure`  
**Package:** `...persistence.entity`  
**File:** `ExerciseEntity.java` — `@Entity @Table(name="exercises")` with:
- `@ElementCollection` for `routines` (→ `exercise_routines`)
- `@ElementCollection` for `secondaryMuscles` (→ `exercise_secondary_muscles`)
- `@ManyToOne(fetch=LAZY)` for `imageEntity` and `videoEntity`
- `@OneToMany(mappedBy="exercise", fetch=LAZY)` for `exerciseSeriesEntities`
- `@SQLRestriction` + `@SQLDelete`
- Embedded `AuditEmbeddable`

**Verify:** `./gradlew :infrastructure:compileJava` succeeds.

---

### Step 41 — Create `WorkoutEntity`, `ExerciseSeriesEntity`, `SeriesEntity` JPA entities

**Module:** `:infrastructure`  
**Package:** `...persistence.entity`

`WorkoutEntity`: `@OneToMany(mappedBy="workout", fetch=LAZY)` exerciseSeriesEntities.  
`ExerciseSeriesEntity`: `@ManyToOne(fetch=LAZY)` to workout + exercise; `@OneToMany(mappedBy="exerciseSeries", fetch=LAZY)` to series.  
`SeriesEntity`: `@ManyToOne(fetch=LAZY)` to exerciseSeries.

All with `@SQLRestriction` + `@SQLDelete` + embedded `AuditEmbeddable`.

**Verify:** `./gradlew :infrastructure:compileJava` succeeds.

---

### Step 42 — Create Spring Data repositories

**Module:** `:infrastructure`  
**Package:** `...persistence.repository`  
**Files:**
- `SpringMediaRepository.java extends JpaRepository<MediaEntity, UUID>`
- `SpringExerciseRepository.java` — with `@EntityGraph` method `findWithAllById`, `existsByName`, paging query for summaries
- `SpringWorkoutRepository.java`
- `SpringExerciseSeriesRepository.java` — `findAllByWorkout_Id`
- `SpringSeriesRepository.java` — `findAllByExerciseSeries_Id`, `countByExerciseSeries_Id` (for serialNumber)

**Verify:** `./gradlew :infrastructure:compileJava` succeeds.

---

### Step 43 — Create `ExercisePersistenceMapper`

**Module:** `:infrastructure`  
**Package:** `...persistence.mapper`  
**File:** `ExercisePersistenceMapper.java` — hand-written mapper (no MapStruct for now, per CLAUDE.md style).

Methods:
- `ExerciseEntity toEntity(Exercise domain)`
- `Exercise toDomain(ExerciseEntity entity)` — uses package-private reconstitution constructor
- `ExerciseDetailView toDetailView(ExerciseEntity entity)`
- `ExerciseSummaryView toSummaryView(ExerciseEntity entity)`

**Verify:** `./gradlew :infrastructure:compileJava` succeeds.

---

### Step 44 — Create `WorkoutPersistenceMapper`, `ExerciseSeriesPersistenceMapper`, `SeriesPersistenceMapper`

**Module:** `:infrastructure`  
**Package:** `...persistence.mapper`

Same pattern as `ExercisePersistenceMapper`, each with `toEntity`, `toDomain`, `toDetailView`, `toSummaryView`.

**Verify:** `./gradlew :infrastructure:compileJava` succeeds.

---

### Step 45 — Create `ExerciseJpaAdapter` and `MediaJpaAdapter`

**Module:** `:infrastructure`  
**Package:** `...persistence`  
**Files:**
- `ExerciseJpaAdapter.java` — `@Component`, implements `ExerciseRepositoryPort` + `ExerciseQueryPort`
- `MediaJpaAdapter.java` — implements `MediaRepositoryPort`

`deleteById` triggers `@SQLDelete` (soft delete). `findAll` uses JPQL projection.

**Verify:** `./gradlew :infrastructure:compileJava` succeeds.

---

### Step 46 — Create `WorkoutJpaAdapter`, `ExerciseSeriesJpaAdapter`, `SeriesJpaAdapter`

**Module:** `:infrastructure`  
**Package:** `...persistence`

Same pattern. `SeriesJpaAdapter.save(Series)` auto-computes `serialNumber` = `countByExerciseSeries + 1` before persisting.

**Verify:** `./gradlew :infrastructure:compileJava` succeeds.

---

### Step 47 — Create `PersistenceConfig` with JPA Auditing

**Module:** `:infrastructure`  
**Package:** `...config`  
**File:** `PersistenceConfig.java`

```java
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class PersistenceConfig {
    @Bean
    public AuditorAware<String> auditorProvider() {
        // For now returns "system"; will be replaced when Spring Security is added
        return () -> Optional.of("system");
    }
}
```

**Verify:** `./gradlew :infrastructure:bootRun` — app starts, all tables created/validated by Flyway.

---

## Phase 5 — Infrastructure Layer: REST Controllers

### Step 48 — Create `PageResponse<T>` record

**Module:** `:infrastructure`  
**Package:** `com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto`  
**File:** `PageResponse.java` — as defined in CLAUDE.md conventions.

**Verify:** `./gradlew :infrastructure:compileJava` succeeds.

---

### Step 49 — Create Exercise REST DTOs (request/response)

**Module:** `:infrastructure`  
**Package:** `...rest.dto`  
**Files:**
- `CreateExerciseRequest.java` (record) — Bean Validation annotations (`@NotBlank`, `@NotNull`, `@Size`, `@Valid`)
- `UpdateExerciseRequest.java` (record)
- `MediaRequest.java` (record) — `name`, `description`, `url` with `@NotBlank url`
- `ExerciseResponse.java` (record) — full detail response
- `ExerciseSummaryResponse.java` (record)

**Verify:** `./gradlew :infrastructure:compileJava` succeeds.

---

### Step 50 — Create `ExerciseRestMapper`

**Module:** `:infrastructure`  
**Package:** `...rest.mapper`  
**File:** `ExerciseRestMapper.java` — maps `CreateExerciseRequest` → `CreateExerciseCommand`, `ExerciseDetailView` → `ExerciseResponse`, `ExerciseSummaryView` → `ExerciseSummaryResponse`, `Page<ExerciseSummaryView>` → `PageResponse<ExerciseSummaryResponse>`.

**Verify:** `./gradlew :infrastructure:compileJava` succeeds.

---

### Step 51 — Create `ExerciseController`

**Module:** `:infrastructure`  
**Package:** `...rest`  
**File:** `ExerciseController.java`

Endpoints:
- `POST /api/v1/exercises` → `201 Created` + `Location` header
- `GET /api/v1/exercises/{id}` → `200 OK`
- `GET /api/v1/exercises` (paginated) → `200 OK`
- `PUT /api/v1/exercises/{id}` → `200 OK`
- `DELETE /api/v1/exercises/{id}` → `204 No Content`

Includes: `@Valid`, `@Idempotency-Key` header on POST/PUT, OpenAPI annotations (`@Tag`, `@Operation`, `@ApiResponse`).

**Verify:** `./gradlew :infrastructure:bootRun` — Swagger UI shows Exercise endpoints at `http://localhost:8080/swagger-ui.html`.

---

### Step 52 — Create Workout REST DTOs and `WorkoutRestMapper`

**Module:** `:infrastructure`  
**Package:** `...rest.dto`, `...rest.mapper`  
**Files:**
- `CreateWorkoutRequest.java`, `FinishWorkoutRequest.java`, `WorkoutResponse.java`, `WorkoutSummaryResponse.java`
- `WorkoutRestMapper.java`

**Verify:** `./gradlew :infrastructure:compileJava` succeeds.

---

### Step 53 — Create `WorkoutController`

Endpoints:
- `POST /api/v1/workouts` → `201 Created`
- `GET /api/v1/workouts/{id}` → `200 OK`
- `GET /api/v1/workouts` (paginated) → `200 OK`
- `PATCH /api/v1/workouts/{id}/finish` → `200 OK`
- `DELETE /api/v1/workouts/{id}` → `204 No Content`

**Verify:** `./gradlew :infrastructure:bootRun` — Swagger UI shows Workout endpoints.

---

### Step 54 — Create ExerciseSeries REST DTOs, Mapper and Controller

Endpoints:
- `POST /api/v1/workouts/{workoutId}/exercises` → adds ExerciseSeries → `201 Created`
- `GET /api/v1/workouts/{workoutId}/exercises` → list ExerciseSeries for workout
- `GET /api/v1/workouts/{workoutId}/exercises/{exerciseSeriesId}` → `200 OK`
- `DELETE /api/v1/workouts/{workoutId}/exercises/{exerciseSeriesId}` → `204 No Content`

**Verify:** `./gradlew :infrastructure:bootRun` — Swagger UI shows ExerciseSeries endpoints.

---

### Step 55 — Create Series REST DTOs, Mapper and Controller

Endpoints:
- `POST /api/v1/workouts/{workoutId}/exercises/{exerciseSeriesId}/series` → `201 Created`
- `GET /api/v1/workouts/{workoutId}/exercises/{exerciseSeriesId}/series` → list
- `GET /api/v1/workouts/{workoutId}/exercises/{exerciseSeriesId}/series/{seriesId}` → `200 OK`
- `PUT /api/v1/workouts/{workoutId}/exercises/{exerciseSeriesId}/series/{seriesId}` → `200 OK`
- `DELETE /api/v1/workouts/{workoutId}/exercises/{exerciseSeriesId}/series/{seriesId}` → `204 No Content`

**Verify:** `./gradlew :infrastructure:bootRun` — Swagger UI shows Series endpoints.

---

## Phase 5 (cont.) — Config Beans and Event Publisher (Steps 56–58)

### Step 56 — Create `ExerciseConfig`

**Module:** `:infrastructure`  
**Package:** `...config`  
**File:** `ExerciseConfig.java`

Register as `@Bean`:
- `CreateExerciseUseCase` → wraps `CreateExerciseHandler` in `TransactionalCreateExerciseUseCase`
- `UpdateExerciseUseCase`
- `DeleteExerciseUseCase`
- `GetExerciseByIdUseCase`
- `ListExercisesUseCase`

> All handlers are instantiated with `new`, NO `@Service` on the handler classes. Transactional wrapper applies `TransactionTemplate`.

**Verify:** `./gradlew :infrastructure:bootRun` — context loads, no `NoSuchBeanDefinitionException`.

---

### Step 57 — Create `WorkoutConfig`, `ExerciseSeriesConfig`, `SeriesConfig`

Same pattern as `ExerciseConfig` for the remaining three aggregates.

**Verify:** `./gradlew :infrastructure:bootRun` — full context loads.

---

### Step 58 — Create `SpringDomainEventPublisher` (DomainEventPublisherPort adapter)

**Module:** `:infrastructure`  
**Package:** `...adapter.output.messaging`  
**File:** `SpringDomainEventPublisher.java` — `@Component implements DomainEventPublisherPort`, delegates to `ApplicationEventPublisher`.

**Verify:** `./gradlew :infrastructure:bootRun` — events published without errors.

---

## Phase 6 — Cross-Cutting Concerns

### Step 59 — Create `GlobalExceptionHandler`

**Module:** `:infrastructure`  
**Package:** `...adapter.input.rest`  
**File:** `GlobalExceptionHandler.java` — `@RestControllerAdvice` mapping all exception categories defined in CLAUDE.md to ProblemDetail responses (RFC 9457).

Handles: `NotFoundException` → 404, `BusinessRuleViolationException` → 422, `ConflictException` → 409, `MethodArgumentNotValidException` → 422 with field violations list.

**Verify:** hitting a non-existent exercise UUID returns `application/problem+json` with `status: 404`.

---

### Step 60 — Create `OpenApiConfig`

**Module:** `:infrastructure`  
**Package:** `...config`  
**File:** `OpenApiConfig.java` — configure `OpenAPI` bean with title "GymRat API", version "v1", description, and `bearerAuth` security scheme placeholder.

Add `springdoc-openapi-starter-webmvc-ui` to `libs.versions.toml` and `infrastructure/build.gradle.kts`.

**Verify:** `http://localhost:8080/swagger-ui.html` opens and shows all endpoints grouped by tag.

---

### Step 61 — Configure structured logging and Micrometer counters

**Module:** `:infrastructure`  
**Files:**
- `infrastructure/src/main/resources/logback-spring.xml` — JSON format for `prod` profile, plain text for `local`
- Add `exercises.created.total`, `workouts.started.total`, `series.recorded.total` counters in respective handlers

**Verify:** `./gradlew :infrastructure:bootRun` — logs appear; `http://localhost:8080/actuator/metrics` lists custom counters.

---

### Step 62 — Create Flyway seed migration (V6)

**File:** `V6__seed_sample_data.sql`

Insert sample data:
- 3 `media` rows (images + video)
- 5 `exercises` (covering different levels, muscles, routines)
- 2 `workouts`
- 3 `exercise_series` rows (linking workouts and exercises)
- 6 `series` rows

**Verify:** `GET /api/v1/exercises` returns paginated results with seed data.  
**Verify:** `GET /api/v1/workouts` returns paginated results.

---

## Phase 7 — Testing

### Step 63 — Domain unit tests: `ExerciseTest`

**Module:** `:domain`  
**File:** `ExerciseTest.java`

Tests:
- `shouldCreateExercise_whenValidData()`
- `shouldEmitExerciseCreatedEvent_whenCreated()`
- `shouldThrowNullPointerException_whenNameIsNull()`
- `shouldUpdateExercise_whenValidData()`
- `shouldEmitExerciseUpdatedEvent_whenUpdated()`

**Verify:** `./gradlew :domain:test` — all green.

---

### Step 64 — Domain unit tests: `WorkoutTest`, `SeriesTest`, `ExerciseSeriesTest`

**Module:** `:domain`

- `WorkoutTest`: create, finish, add exercise series
- `SeriesTest`: create, RPE validation (1-10)
- `ExerciseSeriesTest`: create, add series

**Verify:** `./gradlew :domain:test` — all green.

---

### Step 65 — Application unit tests: `CreateExerciseHandlerTest`

**Module:** `:application`

Mock: `ExerciseRepositoryPort`, `MediaRepositoryPort`, `DomainEventPublisherPort`, `MeterRegistry`.

Tests:
- `shouldReturnExerciseId_whenValidCommand()`
- `shouldThrowDuplicateExerciseNameException_whenNameExists()`
- `shouldPublishExerciseCreatedEvent_whenSuccess()`

**Verify:** `./gradlew :application:test` — all green.

---

### Step 66 — Application unit tests: remaining handlers

**Module:** `:application`

`UpdateExerciseHandlerTest`, `DeleteExerciseHandlerTest`, `CreateWorkoutHandlerTest`, `FinishWorkoutHandlerTest`, `RecordSeriesHandlerTest`, `AddExerciseToWorkoutHandlerTest`.

**Verify:** `./gradlew :application:test` — all green.

---

### Step 67 — Controller slice tests: `ExerciseControllerTest` ✅

**Module:** `:infrastructure`  
**Note:** `@WebMvcTest` was removed in Spring Boot 4.x. Use `MockMvcBuilders.standaloneSetup()` with
`@ExtendWith(MockitoExtension.class)` + `GlobalExceptionHandler` registered via `.setControllerAdvice()`.
Use `JacksonJsonHttpMessageConverter` (Spring Framework 7 replacement for the deprecated `MappingJackson2HttpMessageConverter`).

Tests implemented (`@Mock` use case beans, `@Nested` per HTTP verb):
- **POST** `shouldReturn201_whenCreateExercise()`, `shouldReturn422_whenNameIsBlank()`, `shouldReturn422_whenLevelIsMissing()`, `shouldReturn422_whenRoutinesIsEmpty()`, `shouldReturn409_whenNameAlreadyExists()`
- **GET /{id}** `shouldReturn200_whenExerciseFound()`, `shouldReturn404_whenExerciseNotFound()`, `shouldReturn400_whenIdIsInvalidUuid()`
- **GET** `shouldReturn200WithPage_whenListExercises()`, `shouldReturn200WithEmptyPage_whenNoExercises()`
- **PUT** `shouldReturn200_whenExerciseUpdated()`, `shouldReturn404_whenExerciseNotFound()`, `shouldReturn409_whenNameAlreadyExists()`
- **DELETE** `shouldReturn204_whenDeleteExercise()`, `shouldReturn404_whenExerciseNotFound()`, `shouldReturn400_whenIdIsInvalidUuid()`

**Verify:** `./gradlew :infrastructure:test --tests "*ExerciseControllerTest"` — green.

---

### Step 68 — Controller slice tests: `WorkoutControllerTest`, `SeriesControllerTest` ✅

Same pattern as step 67 (`MockMvcBuilders.standaloneSetup()` + `@ExtendWith(MockitoExtension.class)` + `GlobalExceptionHandler`).

**`WorkoutControllerTest`** — 12 tests across 5 `@Nested` groups:
- **POST** `shouldReturn201_whenCreateWorkout()`, `shouldReturn422_whenStartWorkoutIsMissing()`
- **GET /{id}** `shouldReturn200_whenWorkoutFound()`, `shouldReturn404_whenWorkoutNotFound()`, `shouldReturn400_whenIdIsInvalidUuid()`
- **GET** `shouldReturn200WithPage_whenListWorkouts()`, `shouldReturn200WithEmptyPage_whenNoWorkouts()`
- **PATCH /{id}/finish** `shouldReturn200_whenWorkoutFinished()`, `shouldReturn404_whenWorkoutNotFound()`, `shouldReturn422_whenAlreadyFinished()`
- **DELETE** `shouldReturn204_whenDeleteWorkout()`, `shouldReturn404_whenWorkoutNotFound()`, `shouldReturn400_whenIdIsInvalidUuid()`

**`SeriesControllerTest`** — 14 tests across 5 `@Nested` groups:
- **POST** `shouldReturn201_whenSeriesRecorded()`, `shouldReturn422_whenRepetitionsToDoIsZero()`, `shouldReturn422_whenRestTimeIsMissing()`, `shouldReturn404_whenExerciseSeriesNotFound()`
- **GET** `shouldReturn200_whenSeriesExist()`, `shouldReturn200_whenNoSeriesExist()`
- **GET /{seriesId}** `shouldReturn200_whenSeriesFound()`, `shouldReturn404_whenSeriesNotFound()`, `shouldReturn400_whenSeriesIdIsInvalidUuid()`
- **PUT /{seriesId}** `shouldReturn200_whenSeriesUpdated()`, `shouldReturn404_whenSeriesNotFound()`
- **DELETE /{seriesId}** `shouldReturn204_whenSeriesDeleted()`, `shouldReturn404_whenSeriesNotFound()`, `shouldReturn400_whenSeriesIdIsInvalidUuid()`

**Verify:** `./gradlew :infrastructure:test` — all controller tests green.

---

### Step 69 — JPA Adapter integration tests with Testcontainers ✅

**Module:** `:infrastructure`  
**Approach:** `@SpringBootTest` + `@Testcontainers` + `@Transactional` (rollback per test).  
Note: `@DataJpaTest` was not used because it bypasses Flyway migrations and the multi-module setup;
`@SpringBootTest` against a real Testcontainers PostgreSQL is the correct approach.

**Important:** Exercise `name` is `UNIQUE` in the DB and `data.sql` seeds sample data.
Helper methods append `System.nanoTime()` to exercise names to avoid constraint collisions.

**`ExerciseJpaAdapterTest`** — 16 tests across 6 `@Nested` groups:
- **save()** — persists with same id, routines element-collection, secondaryMuscles, active audit fields
- **findById()** — found, not found
- **findAll()** — paged list, respects page size, empty page
- **existsByName()** — true when exists, false when not
- **deleteById() soft-delete** — not returned by findById, excluded from paged result
- **findDetailById()** — detail view with all fields

**`SeriesJpaAdapterTest`** — 13 tests across 6 `@Nested` groups:
- **save()** — persists with same id, intensity+weight, active audit fields
- **findById()** — found, not found
- **serialNumber ordering** — count=0 before any, increments per save, ordered asc
- **findAllByExerciseSeriesId()** — empty list, all series returned
- **deleteById() soft-delete** — not returned by findById, count decremented
- **findDetailById()** — detail view with all fields

**Verify:** `./gradlew :infrastructure:test --tests "*JpaAdapterTest"` — green.

---

### Step 70 — ArchUnit architecture tests

**Module:** `:infrastructure`  
**File:** `ArchitectureTest.java`

Rules:
- `domainIsIsolated` — no Spring/JPA imports in domain
- `applicationDoesNotUseInfrastructure`
- `springAnnotationsOnlyInInfrastructure`
- `commandHandlersImplementCommandPorts`
- `queryHandlersImplementQueryPorts`
- `controllersOnlyDependOnUseCasePorts` — never on concrete Handler classes

**Verify:** `./gradlew :infrastructure:test --tests "*ArchitectureTest"` — all rules pass.

---

---

## Phase 8 — End-to-End: Integration, Security & Deployment

### Step 71 — Full integration test: Exercise happy path

**Module:** `:infrastructure`  
**File:** `ExerciseIntegrationTest.java`  
**Annotation:** `@SpringBootTest(webEnvironment = RANDOM_PORT)` + Testcontainers PostgreSQL

Tests (real HTTP via `TestRestTemplate`):
- `shouldCreateAndRetrieveExercise_whenValidRequest()`
- `shouldReturn409_whenDuplicateExerciseName()`
- `shouldSoftDeleteExercise_andNotReturnItAfterwards()`

**Verify:** `./gradlew :infrastructure:test --tests "*ExerciseIntegrationTest"` — green.

---

### Step 72 — Full integration test: Workout + ExerciseSeries + Series happy path

**Module:** `:infrastructure`  
**File:** `WorkoutIntegrationTest.java`

Full flow:
1. Create a Workout → `201`
2. Add an Exercise to the Workout (ExerciseSeries) → `201`
3. Record 3 Series within the ExerciseSeries → `201` each
4. Verify `GET /api/v1/workouts/{id}` returns the full workout with exercise series
5. Verify `serialNumber` is auto-incremented (1, 2, 3)
6. Finish the Workout → `200`

**Verify:** `./gradlew :infrastructure:test --tests "*WorkoutIntegrationTest"` — green.

---

### Step 73 — Full integration test: soft-delete cascade behaviour

**Module:** `:infrastructure`  
**File:** `SoftDeleteIntegrationTest.java`

Tests:
- Deleting a Workout soft-deletes it; `GET /api/v1/workouts/{id}` returns `404`
- Deleting an Exercise soft-deletes it; exercises still retrievable by ID before delete
- Deleted resources do not appear in paginated list responses

**Verify:** `./gradlew :infrastructure:test --tests "*SoftDeleteIntegrationTest"` — green.

---

### Step 74 — Validation integration tests

**Module:** `:infrastructure`  
**File:** `ValidationIntegrationTest.java`

Tests:
- `POST /api/v1/exercises` with blank name → `422` with `application/problem+json` body listing field errors
- `POST /api/v1/exercises` with invalid level enum → `400`
- `POST /api/v1/workouts/{workoutId}/exercises/{esId}/series` with `intensity=11` → `422`
- `PATCH /api/v1/workouts/{id}/finish` when already finished → `422`

**Verify:** `./gradlew :infrastructure:test --tests "*ValidationIntegrationTest"` — green.

---

### Step 75 — Pagination and sorting integration tests

**Module:** `:infrastructure`  
**File:** `PaginationIntegrationTest.java`

Uses seed data (V6 migration).

Tests:
- `GET /api/v1/exercises?page=0&size=2` → returns 2 items, `totalElements >= 5`
- `GET /api/v1/exercises?page=0&size=10&sortBy=name&ascending=true` → results in alphabetical order
- `GET /api/v1/workouts?page=0&size=1` → returns 1 item
- Empty page beyond total → `content: []`, no error

**Verify:** `./gradlew :infrastructure:test --tests "*PaginationIntegrationTest"` — green.

---

### Step 76 — Add Spring Security basic authentication skeleton

**Module:** `:infrastructure`  
**Package:** `...config`  
**File:** `SecurityConfig.java`

- Add `spring-boot-starter-security` to `infrastructure/build.gradle.kts`
- `SecurityConfig` with `SecurityFilterChain`: permit `GET /actuator/health`, `GET /swagger-ui/**`, `GET /v3/api-docs/**`; require HTTP Basic for all other endpoints
- Replace `auditorProvider` placeholder: read principal name from `SecurityContextHolder`
- `SecurityConfig` declares an in-memory user (`gymrat` / `gymrat`) for local dev

**Verify:** `./gradlew :infrastructure:bootRun` — unauthenticated `GET /api/v1/exercises` returns `401`.

---

### Step 77 — Update controller and integration tests for authentication

**Module:** `:infrastructure`

- Update `@WebMvcTest` controller tests: add `@WithMockUser` or `Authorization: Basic` header
- Update `ExerciseIntegrationTest`, `WorkoutIntegrationTest`, `SoftDeleteIntegrationTest`, `ValidationIntegrationTest`, `PaginationIntegrationTest`: set credentials on `TestRestTemplate`

**Verify:** `./gradlew :infrastructure:test` — all tests green with security enabled.

---

### Step 78 — Dockerfile and docker-compose production profile

**Files:**
- `Dockerfile` (root) — multi-stage build: `gradle build` → `eclipse-temurin:21-jre` runtime image
- `compose.yaml` — add `app` service referencing the image, linked to `postgres`; `POSTGRES_*` and `SPRING_DATASOURCE_*` via env vars

**Verify:** `docker compose up --build` — app starts, `GET http://localhost:8080/actuator/health` returns `{"status":"UP"}`.

---

### Step 79 — Add GitHub Actions CI pipeline

**File:** `.github/workflows/ci.yml`

Stages:
1. `build` — `./gradlew build` (compileJava + test all modules)
2. `docker` (on `main`) — build Docker image, push to GitHub Container Registry

**Verify:** push to `main` branch → GitHub Actions pipeline passes green.

---

### Step 80 — Performance baseline and README

**Files:**
- `README.md` — project overview, local setup instructions, environment variables reference, API quick-start examples
- Add `spring.jpa.properties.hibernate.generate_statistics=false` production guard
- Verify N+1 queries are absent: enable `spring.jpa.show-sql=true` locally, run `GET /api/v1/exercises` and `GET /api/v1/workouts/{id}`, confirm no unexpected extra queries

**Verify:** `GET /api/v1/exercises` with 5 seed exercises produces exactly **1 SQL query**; `GET /api/v1/workouts/{id}` produces at most **3 queries** (workout + exerciseSeries + series). All tests still green.

---

## Quick Reference: Package Structure

```
domain/src/main/java/com/n1b3lung0/gymrat/
  domain/
    model/          ← Exercise, Workout, ExerciseSeries, Series, all enums, all VOs, AuditFields
    event/          ← ExerciseEvent, WorkoutEvent, SeriesEvent (sealed + records)
    exception/      ← DomainException hierarchy + concrete exceptions
    repository/     ← *RepositoryPort interfaces (Output Ports of domain)

application/src/main/java/com/n1b3lung0/gymrat/
  application/
    port/
      input/
        command/    ← *UseCase interfaces (write side)
        query/      ← *UseCase interfaces (read side)
      output/       ← *QueryPort, DomainEventPublisherPort
    command/        ← *Handler (write)
    query/          ← *Handler (read)
    dto/            ← Commands, Queries, *View records

infrastructure/src/main/java/com/n1b3lung0/gymrat/
  infrastructure/
    adapter/
      input/
        rest/       ← *Controller, GlobalExceptionHandler
        rest/dto/   ← *Request, *Response, PageResponse
        rest/mapper/← *RestMapper
      output/
        persistence/        ← *JpaAdapter
        persistence/entity/ ← *Entity, AuditEmbeddable
        persistence/mapper/ ← *PersistenceMapper
        persistence/repository/ ← Spring*Repository
        messaging/          ← SpringDomainEventPublisher
    config/         ← *Config (bean registration), PersistenceConfig, OpenApiConfig
```

---

## Key Design Decisions Summary

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Media deduplication | Separate `media` table, FK from `exercises` | Avoids URL duplication; multiple exercises can share same image |
| exerciseSeries | Dedicated entity/table | Acts as join + container for Series; carries its own lifecycle |
| serialNumber auto-increment | Count existing series + 1 in adapter | Simple, no gap on delete is acceptable for display order |
| AuditFields in domain | Yes, as Value Object | Business needs active/inactive and soft-delete reasoning |
| Soft delete | `@SQLRestriction("deleted_at IS NULL")` | Transparent filtering at JPA level; domain unaware |
| Routines constraint | `UNIQUE(workout_id, exercise_id)` in DB | Enforced at DB level; domain rule mirrored in `ConflictException` |
| IDs | UUID everywhere | No sequential ID exposure; globally unique |
| Transactions | `TransactionTemplate` decorator in Config | `@Transactional` never on application/domain classes |
| Event publishing | Spring `ApplicationEventPublisher` | Simple, synchronous, no Kafka needed at this stage |
```


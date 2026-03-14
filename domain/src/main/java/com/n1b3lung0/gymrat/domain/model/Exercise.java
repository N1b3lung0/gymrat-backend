package com.n1b3lung0.gymrat.domain.model;

import com.n1b3lung0.gymrat.domain.event.ExerciseCreated;
import com.n1b3lung0.gymrat.domain.event.ExerciseDeleted;
import com.n1b3lung0.gymrat.domain.event.ExerciseUpdated;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Aggregate root representing a physical exercise.
 *
 * <p>An exercise belongs to one or more {@link Routine}s, targets a primary
 * {@link Muscle} and optionally a set of secondary muscles. It may carry
 * optional media assets (image, video) and tracks which {@link ExerciseSeries}
 * sessions have used it (by ID reference only — DDD cross-aggregate rule).
 *
 * <p>Use {@link Builder} to create a new instance or reconstitute from persistence.
 */
public class Exercise {

    private final ExerciseId id;
    private String name;
    private String description;
    private Level level;
    private Set<Routine> routines;
    private Muscle primaryMuscle;
    private Set<Muscle> secondaryMuscles;
    private Media image;
    private Media video;
    private final List<ExerciseSeriesId> exerciseSeriesIds;
    private AuditFields auditFields;

    /** Domain events accumulated during this session; drained after persistence. */
    private final List<Object> domainEvents = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Package-private reconstitution constructor — for the persistence mapper only
    // -------------------------------------------------------------------------

    Exercise(
            ExerciseId id,
            String name,
            String description,
            Level level,
            Set<Routine> routines,
            Muscle primaryMuscle,
            Set<Muscle> secondaryMuscles,
            Media image,
            Media video,
            List<ExerciseSeriesId> exerciseSeriesIds,
            AuditFields auditFields) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.level = level;
        this.routines = EnumSet.copyOf(routines);
        this.primaryMuscle = primaryMuscle;
        this.secondaryMuscles = secondaryMuscles == null || secondaryMuscles.isEmpty()
                ? EnumSet.noneOf(Muscle.class)
                : EnumSet.copyOf(secondaryMuscles);
        this.image = image;
        this.video = video;
        this.exerciseSeriesIds = new ArrayList<>(exerciseSeriesIds);
        this.auditFields = auditFields;
    }

    // -------------------------------------------------------------------------
    // Factory method — business entry point
    // -------------------------------------------------------------------------

    /**
     * Creates a new {@code Exercise} and emits an {@link ExerciseCreated} event.
     *
     * <p>Prefer {@link Builder} to supply the arguments fluently.
     */
    public static Exercise create(
            String name,
            String description,
            Level level,
            Set<Routine> routines,
            Muscle primaryMuscle,
            Set<Muscle> secondaryMuscles,
            Media image,
            Media video) {

        Objects.requireNonNull(name, "Exercise name must not be null");
        Objects.requireNonNull(level, "Exercise level must not be null");
        Objects.requireNonNull(primaryMuscle, "Exercise primaryMuscle must not be null");
        if (name.isBlank()) throw new IllegalArgumentException("Exercise name must not be blank");
        if (routines == null || routines.isEmpty())
            throw new IllegalArgumentException("Exercise must belong to at least one routine");

        var exercise = new Exercise(
                ExerciseId.generate(),
                name,
                description,
                level,
                routines,
                primaryMuscle,
                secondaryMuscles == null ? Set.of() : secondaryMuscles,
                image,
                video,
                new ArrayList<>(),
                AuditFields.create("system")
        );
        exercise.domainEvents.add(new ExerciseCreated(exercise.id, exercise.name));
        return exercise;
    }

    // -------------------------------------------------------------------------
    // Business methods
    // -------------------------------------------------------------------------

    /**
     * Updates the exercise with new field values.
     *
     * @param name             new name; must not be blank
     * @param description      new description
     * @param level            new difficulty level
     * @param routines         new routine set; must not be empty
     * @param primaryMuscle    new primary muscle
     * @param secondaryMuscles new secondary muscles (may be empty)
     * @param image            new image asset (nullable)
     * @param video            new video asset (nullable)
     */
    public void update(
            String name,
            String description,
            Level level,
            Set<Routine> routines,
            Muscle primaryMuscle,
            Set<Muscle> secondaryMuscles,
            Media image,
            Media video) {

        Objects.requireNonNull(name, "Exercise name must not be null");
        Objects.requireNonNull(level, "Exercise level must not be null");
        Objects.requireNonNull(primaryMuscle, "Exercise primaryMuscle must not be null");
        if (name.isBlank()) throw new IllegalArgumentException("Exercise name must not be blank");
        if (routines == null || routines.isEmpty())
            throw new IllegalArgumentException("Exercise must belong to at least one routine");

        this.name = name;
        this.description = description;
        this.level = level;
        this.routines = EnumSet.copyOf(routines);
        this.primaryMuscle = primaryMuscle;
        this.secondaryMuscles = secondaryMuscles == null || secondaryMuscles.isEmpty()
                ? EnumSet.noneOf(Muscle.class)
                : EnumSet.copyOf(secondaryMuscles);
        this.image = image;
        this.video = video;
        this.auditFields = this.auditFields.update("system");
        domainEvents.add(new ExerciseUpdated(this.id));
    }

    /**
     * Soft-deletes this exercise and emits an {@link ExerciseDeleted} event.
     */
    public void delete() {
        this.auditFields = this.auditFields.delete("system");
        domainEvents.add(new ExerciseDeleted(this.id));
    }

    /**
     * Registers an {@link ExerciseSeriesId} reference on this exercise.
     *
     * @param exerciseSeriesId the ID of the ExerciseSeries that uses this exercise
     */
    public void addExerciseSeries(ExerciseSeriesId exerciseSeriesId) {
        Objects.requireNonNull(exerciseSeriesId, "exerciseSeriesId must not be null");
        if (!exerciseSeriesIds.contains(exerciseSeriesId)) {
            exerciseSeriesIds.add(exerciseSeriesId);
        }
    }

    /**
     * Drains and returns all accumulated domain events.
     * Must be called by the application layer after persisting the aggregate.
     *
     * @return immutable snapshot of pending events; the internal list is cleared
     */
    public List<Object> pullDomainEvents() {
        var events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }

    // -------------------------------------------------------------------------
    // Getters — no setters; state changes go through business methods
    // -------------------------------------------------------------------------

    public ExerciseId getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Level getLevel() { return level; }
    public Set<Routine> getRoutines() { return Collections.unmodifiableSet(routines); }
    public Muscle getPrimaryMuscle() { return primaryMuscle; }
    public Set<Muscle> getSecondaryMuscles() { return Collections.unmodifiableSet(secondaryMuscles); }
    public Media getImage() { return image; }
    public Media getVideo() { return video; }
    public List<ExerciseSeriesId> getExerciseSeriesIds() { return Collections.unmodifiableList(exerciseSeriesIds); }
    public AuditFields getAuditFields() { return auditFields; }

    // -------------------------------------------------------------------------
    // Builder — preferred API for creation with many optional fields
    // -------------------------------------------------------------------------

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private String name;
        private String description;
        private Level level;
        private Set<Routine> routines;
        private Muscle primaryMuscle;
        private Set<Muscle> secondaryMuscles;
        private Media image;
        private Media video;

        private Builder() {}

        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder level(Level level) { this.level = level; return this; }
        public Builder routines(Set<Routine> routines) { this.routines = routines; return this; }
        public Builder primaryMuscle(Muscle primaryMuscle) { this.primaryMuscle = primaryMuscle; return this; }
        public Builder secondaryMuscles(Set<Muscle> secondaryMuscles) { this.secondaryMuscles = secondaryMuscles; return this; }
        public Builder image(Media image) { this.image = image; return this; }
        public Builder video(Media video) { this.video = video; return this; }

        /** Applies invariants and delegates to {@link Exercise#create}. */
        public Exercise build() {
            return Exercise.create(name, description, level, routines, primaryMuscle, secondaryMuscles, image, video);
        }
    }
}


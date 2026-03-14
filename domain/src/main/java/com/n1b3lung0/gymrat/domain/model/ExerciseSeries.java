package com.n1b3lung0.gymrat.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate representing a single exercise session inside a {@link Workout}.
 *
 * <p>Acts as the join entity between {@link Workout}, {@link Exercise} and
 * {@link Series}. It holds references by ID only — no cross-aggregate object
 * graph traversal (DDD rule).
 *
 * <pre>
 * Workout → has many ExerciseSeries
 * ExerciseSeries → belongs to one Exercise, contains many Series
 * </pre>
 */
public class ExerciseSeries {

    private final ExerciseSeriesId id;
    private final WorkoutId workoutId;
    private final ExerciseId exerciseId;
    private final List<SeriesId> seriesIds;
    private AuditFields auditFields;

    /** Domain events accumulated during this session; drained after persistence. */
    private final List<Object> domainEvents = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Package-private reconstitution constructor — for the persistence mapper only
    // -------------------------------------------------------------------------

    ExerciseSeries(
            ExerciseSeriesId id,
            WorkoutId workoutId,
            ExerciseId exerciseId,
            List<SeriesId> seriesIds,
            AuditFields auditFields) {
        this.id = id;
        this.workoutId = workoutId;
        this.exerciseId = exerciseId;
        this.seriesIds = new ArrayList<>(seriesIds);
        this.auditFields = auditFields;
    }

    // -------------------------------------------------------------------------
    // Factory method — business entry point
    // -------------------------------------------------------------------------

    /**
     * Creates a new {@code ExerciseSeries} linking an exercise to a workout.
     *
     * @param workoutId  the parent workout
     * @param exerciseId the exercise being performed
     * @return a new {@code ExerciseSeries} with no series yet
     */
    public static ExerciseSeries create(WorkoutId workoutId, ExerciseId exerciseId) {
        Objects.requireNonNull(workoutId, "ExerciseSeries workoutId must not be null");
        Objects.requireNonNull(exerciseId, "ExerciseSeries exerciseId must not be null");

        return new ExerciseSeries(
                ExerciseSeriesId.generate(),
                workoutId,
                exerciseId,
                new ArrayList<>(),
                AuditFields.create("system")
        );
    }

    // -------------------------------------------------------------------------
    // Business methods
    // -------------------------------------------------------------------------

    /**
     * Registers a new {@link Series} reference on this exercise session.
     *
     * @param seriesId the ID of the series to add; must not be null or duplicate
     */
    public void addSeries(SeriesId seriesId) {
        Objects.requireNonNull(seriesId, "seriesId must not be null");
        if (seriesIds.contains(seriesId)) {
            throw new IllegalArgumentException(
                    "Series " + seriesId + " is already registered in ExerciseSeries " + id);
        }
        seriesIds.add(seriesId);
        this.auditFields = this.auditFields.update("system");
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
    // Getters
    // -------------------------------------------------------------------------

    public ExerciseSeriesId getId() { return id; }
    public WorkoutId getWorkoutId() { return workoutId; }
    public ExerciseId getExerciseId() { return exerciseId; }
    public List<SeriesId> getSeriesIds() { return Collections.unmodifiableList(seriesIds); }
    public AuditFields getAuditFields() { return auditFields; }
}


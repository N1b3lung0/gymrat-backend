package com.n1b3lung0.gymrat.domain.model;

import com.n1b3lung0.gymrat.domain.event.WorkoutFinished;
import com.n1b3lung0.gymrat.domain.event.WorkoutStarted;
import com.n1b3lung0.gymrat.domain.exception.WorkoutAlreadyFinishedException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate root representing a single training session.
 *
 * <p>A workout groups one or more {@link ExerciseSeries} sessions. It tracks
 * its own start and end timestamps and references its exercise sessions by ID
 * only (DDD cross-aggregate rule).
 */
public class Workout {

    private final WorkoutId id;
    private final Instant startWorkout;
    private Instant endWorkout;
    private final List<ExerciseSeriesId> exerciseSeriesIds;
    private AuditFields auditFields;

    /** Domain events accumulated during this session; drained after persistence. */
    private final List<Object> domainEvents = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Package-private reconstitution constructor — for the persistence mapper only
    // -------------------------------------------------------------------------

    Workout(
            WorkoutId id,
            Instant startWorkout,
            Instant endWorkout,
            List<ExerciseSeriesId> exerciseSeriesIds,
            AuditFields auditFields) {
        this.id = id;
        this.startWorkout = startWorkout;
        this.endWorkout = endWorkout;
        this.exerciseSeriesIds = new ArrayList<>(exerciseSeriesIds);
        this.auditFields = auditFields;
    }

    // -------------------------------------------------------------------------
    // Factory method — business entry point
    // -------------------------------------------------------------------------

    /**
     * Starts a new {@code Workout}.
     *
     * @param startWorkout timestamp when the session begins; must not be null
     * @return a new open {@code Workout} with no exercise series yet
     */
    public static Workout create(Instant startWorkout) {
        Objects.requireNonNull(startWorkout, "Workout startWorkout must not be null");

        var workout = new Workout(
                WorkoutId.generate(),
                startWorkout,
                null,
                new ArrayList<>(),
                AuditFields.create("system")
        );
        workout.domainEvents.add(new WorkoutStarted(workout.id, workout.startWorkout));
        return workout;
    }

    // -------------------------------------------------------------------------
    // Business methods
    // -------------------------------------------------------------------------

    /**
     * Marks the workout as finished.
     *
     * @param endWorkout timestamp when the session ends; must be after {@code startWorkout}
     * @throws IllegalArgumentException if {@code endWorkout} is before {@code startWorkout}
     * @throws IllegalStateException    if the workout is already finished
     */
    public void finish(Instant endWorkout) {
        Objects.requireNonNull(endWorkout, "endWorkout must not be null");
        if (this.endWorkout != null)
            throw new WorkoutAlreadyFinishedException(id);
        if (endWorkout.isBefore(startWorkout))
            throw new IllegalArgumentException("endWorkout must not be before startWorkout");

        this.endWorkout = endWorkout;
        this.auditFields = this.auditFields.update("system");
        domainEvents.add(new WorkoutFinished(this.id, this.endWorkout));
    }

    /**
     * Registers an {@link ExerciseSeriesId} reference on this workout.
     *
     * @param exerciseSeriesId the ID of the exercise session to add; must not be duplicate
     * @throws IllegalStateException if the workout is already finished
     */
    public void addExerciseSeries(ExerciseSeriesId exerciseSeriesId) {
        Objects.requireNonNull(exerciseSeriesId, "exerciseSeriesId must not be null");
        if (this.endWorkout != null)
            throw new WorkoutAlreadyFinishedException(id);
        if (exerciseSeriesIds.contains(exerciseSeriesId))
            throw new IllegalArgumentException(
                    "ExerciseSeries " + exerciseSeriesId + " is already part of workout " + id);

        exerciseSeriesIds.add(exerciseSeriesId);
        this.auditFields = this.auditFields.update("system");
    }

    /** Returns {@code true} if the workout has been finished. */
    public boolean isFinished() {
        return endWorkout != null;
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

    public WorkoutId getId() { return id; }
    public Instant getStartWorkout() { return startWorkout; }
    public Instant getEndWorkout() { return endWorkout; }
    public List<ExerciseSeriesId> getExerciseSeriesIds() { return Collections.unmodifiableList(exerciseSeriesIds); }
    public AuditFields getAuditFields() { return auditFields; }
}


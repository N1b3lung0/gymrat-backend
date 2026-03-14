package com.n1b3lung0.gymrat.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate representing a single set performed within an {@link ExerciseSeries}.
 *
 * <p>{@code serialNumber} is the 1-based order of the set within its parent
 * {@link ExerciseSeries} and is auto-assigned at creation time by the application
 * layer (based on {@code countByExerciseSeriesId} from the repository port).
 *
 * <p>{@code intensity} follows the RPE scale (1–10).
 */
public class Series {

    private final SeriesId id;
    private final int serialNumber;
    private final int repetitionsToDo;
    private Integer repetitionsDone;
    private final int intensity;
    private final BigDecimal weight;
    private Instant startSeries;
    private Instant endSeries;
    private final RestTime restTime;
    private final ExerciseSeriesId exerciseSeriesId;
    private AuditFields auditFields;

    /** Domain events accumulated during this session; drained after persistence. */
    private final List<Object> domainEvents = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Package-private reconstitution constructor — for the persistence mapper only
    // -------------------------------------------------------------------------

    Series(
            SeriesId id,
            int serialNumber,
            int repetitionsToDo,
            Integer repetitionsDone,
            int intensity,
            BigDecimal weight,
            Instant startSeries,
            Instant endSeries,
            RestTime restTime,
            ExerciseSeriesId exerciseSeriesId,
            AuditFields auditFields) {
        this.id = id;
        this.serialNumber = serialNumber;
        this.repetitionsToDo = repetitionsToDo;
        this.repetitionsDone = repetitionsDone;
        this.intensity = intensity;
        this.weight = weight;
        this.startSeries = startSeries;
        this.endSeries = endSeries;
        this.restTime = restTime;
        this.exerciseSeriesId = exerciseSeriesId;
        this.auditFields = auditFields;
    }

    // -------------------------------------------------------------------------
    // Factory method — business entry point
    // -------------------------------------------------------------------------

    /**
     * Creates a new {@code Series}.
     *
     * @param serialNumber     1-based order within the parent {@link ExerciseSeries}
     * @param repetitionsToDo  planned repetitions; must be positive
     * @param intensity        RPE value between 1 and 10 (inclusive)
     * @param weight           load in kilograms; may be {@code null} for bodyweight sets
     * @param restTime         rest period after this set
     * @param exerciseSeriesId parent {@link ExerciseSeries} reference
     * @return a new {@code Series} instance
     */
    public static Series create(
            int serialNumber,
            int repetitionsToDo,
            int intensity,
            BigDecimal weight,
            RestTime restTime,
            ExerciseSeriesId exerciseSeriesId) {

        Objects.requireNonNull(restTime, "Series restTime must not be null");
        Objects.requireNonNull(exerciseSeriesId, "Series exerciseSeriesId must not be null");
        if (repetitionsToDo <= 0)
            throw new IllegalArgumentException("repetitionsToDo must be positive");
        if (intensity < 1 || intensity > 10)
            throw new IllegalArgumentException("intensity must be between 1 and 10 (RPE scale)");
        if (serialNumber <= 0)
            throw new IllegalArgumentException("serialNumber must be positive");

        return new Series(
                SeriesId.generate(),
                serialNumber,
                repetitionsToDo,
                null,
                intensity,
                weight,
                null,
                null,
                restTime,
                exerciseSeriesId,
                AuditFields.create("system")
        );
    }

    // -------------------------------------------------------------------------
    // Business methods
    // -------------------------------------------------------------------------

    /**
     * Records the start of this set.
     *
     * @param startSeries timestamp when the set begins
     */
    public void start(Instant startSeries) {
        Objects.requireNonNull(startSeries, "startSeries must not be null");
        this.startSeries = startSeries;
        this.auditFields = this.auditFields.update("system");
    }

    /**
     * Records the completion of this set.
     *
     * @param endSeries       timestamp when the set ends
     * @param repetitionsDone actual repetitions performed
     */
    public void finish(Instant endSeries, int repetitionsDone) {
        Objects.requireNonNull(endSeries, "endSeries must not be null");
        if (repetitionsDone < 0)
            throw new IllegalArgumentException("repetitionsDone must not be negative");
        this.endSeries = endSeries;
        this.repetitionsDone = repetitionsDone;
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

    public SeriesId getId() { return id; }
    public int getSerialNumber() { return serialNumber; }
    public int getRepetitionsToDo() { return repetitionsToDo; }
    public Integer getRepetitionsDone() { return repetitionsDone; }
    public int getIntensity() { return intensity; }
    public BigDecimal getWeight() { return weight; }
    public Instant getStartSeries() { return startSeries; }
    public Instant getEndSeries() { return endSeries; }
    public RestTime getRestTime() { return restTime; }
    public ExerciseSeriesId getExerciseSeriesId() { return exerciseSeriesId; }
    public AuditFields getAuditFields() { return auditFields; }
}


package com.n1b3lung0.gymrat.domain.event;

import com.n1b3lung0.gymrat.domain.model.ExerciseSeriesId;
import com.n1b3lung0.gymrat.domain.model.SeriesId;

import java.time.Instant;

/**
 * Emitted when a new {@code Series} set is created within an {@code ExerciseSeries}.
 *
 * @param seriesId         identifier of the created series
 * @param exerciseSeriesId identifier of the parent exercise-series
 * @param serialNumber     1-based order of this set within the exercise-series
 * @param occurredOn       timestamp when the event occurred
 */
public record SeriesCreated(
        SeriesId seriesId,
        ExerciseSeriesId exerciseSeriesId,
        int serialNumber,
        Instant occurredOn)
        implements SeriesEvent {

    public SeriesCreated(SeriesId seriesId, ExerciseSeriesId exerciseSeriesId, int serialNumber) {
        this(seriesId, exerciseSeriesId, serialNumber, Instant.now());
    }
}


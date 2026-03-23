package com.n1b3lung0.gymrat.application.dto;

import com.n1b3lung0.gymrat.domain.model.RestTime;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Full detail view for a single {@code Series}.
 *
 * @param id               series UUID
 * @param serialNumber     1-based order within the exercise-series
 * @param repetitionsToDo  planned repetitions
 * @param repetitionsDone  actual repetitions performed; {@code null} until finished
 * @param intensity        RPE value (1–10)
 * @param weight           load in kg; {@code null} for bodyweight sets
 * @param startSeries      timestamp when the set started; {@code null} until started
 * @param endSeries        timestamp when the set ended; {@code null} until finished
 * @param restTime         rest period after this set
 * @param exerciseSeriesId parent exercise-series UUID
 */
public record SeriesDetailView(
        UUID id,
        int serialNumber,
        int repetitionsToDo,
        Integer repetitionsDone,
        int intensity,
        BigDecimal weight,
        Instant startSeries,
        Instant endSeries,
        RestTime restTime,
        UUID exerciseSeriesId
) {}


package com.n1b3lung0.gymrat.application.dto;

import com.n1b3lung0.gymrat.domain.model.RestTime;
import com.n1b3lung0.gymrat.domain.model.SeriesId;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Command to update an existing series set.
 *
 * @param id               identifier of the series to update
 * @param repetitionsToDo  new planned repetitions; must be positive
 * @param repetitionsDone  actual repetitions performed; {@code null} if not yet done
 * @param intensity        new RPE value 1–10
 * @param weight           new load in kg; {@code null} for bodyweight sets
 * @param startSeries      timestamp when the set started; {@code null} if not started
 * @param endSeries        timestamp when the set ended; {@code null} if not finished
 * @param restTime         new rest period
 */
public record UpdateSeriesCommand(
        SeriesId id,
        int repetitionsToDo,
        Integer repetitionsDone,
        int intensity,
        BigDecimal weight,
        Instant startSeries,
        Instant endSeries,
        RestTime restTime
) {}


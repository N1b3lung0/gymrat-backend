package com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto;

import com.n1b3lung0.gymrat.domain.model.RestTime;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Lightweight REST response DTO for Series listings within an exercise-series.
 *
 * <p>Omits timestamps and {@code repetitionsDone} to keep list responses lean.
 * Use {@link SeriesResponse} for full detail.
 *
 * @param id              series UUID
 * @param serialNumber    1-based order within the exercise-series
 * @param repetitionsToDo planned repetitions
 * @param intensity       RPE value (1–10)
 * @param weight          load in kg; {@code null} for bodyweight sets
 * @param restTime        rest period after this set
 */
public record SeriesSummaryResponse(
        UUID id,
        int serialNumber,
        int repetitionsToDo,
        int intensity,
        BigDecimal weight,
        RestTime restTime
) {}


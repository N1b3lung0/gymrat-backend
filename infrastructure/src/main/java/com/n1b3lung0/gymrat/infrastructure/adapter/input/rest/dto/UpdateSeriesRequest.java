package com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto;

import com.n1b3lung0.gymrat.domain.model.RestTime;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * REST request DTO for updating an existing series set (full replacement — PUT semantics).
 *
 * @param repetitionsToDo  new planned repetitions; must be positive
 * @param repetitionsDone  actual repetitions performed; {@code null} if not yet done; if present must be >= 0
 * @param intensity        new RPE value 1–10; must not be null
 * @param weight           new load in kg; {@code null} for bodyweight sets; if present must be > 0
 * @param startSeries      timestamp when the set started; {@code null} if not started
 * @param endSeries        timestamp when the set ended; {@code null} if not finished
 * @param restTime         new rest period; must not be null
 */
public record UpdateSeriesRequest(
        @Positive(message = "repetitionsToDo must be positive")
        int repetitionsToDo,

        @PositiveOrZero(message = "repetitionsDone must be zero or positive")
        Integer repetitionsDone,

        @NotNull(message = "intensity must not be null")
        @Min(value = 1, message = "intensity must be at least 1 (RPE scale)")
        @Max(value = 10, message = "intensity must be at most 10 (RPE scale)")
        Integer intensity,

        @DecimalMin(value = "0.0", inclusive = false, message = "weight must be greater than 0")
        BigDecimal weight,

        Instant startSeries,

        Instant endSeries,

        @NotNull(message = "restTime must not be null")
        RestTime restTime
) {}


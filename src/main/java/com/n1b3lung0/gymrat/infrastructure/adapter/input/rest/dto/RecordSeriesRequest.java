package com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto;

import com.n1b3lung0.gymrat.domain.model.RestTime;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * REST request DTO for recording a new series set within an exercise-series.
 *
 * @param repetitionsToDo planned repetitions; must be positive
 * @param intensity       RPE value 1–10; must not be null
 * @param weight          load in kg; {@code null} for bodyweight sets; if present must be > 0
 * @param restTime        rest period after this set; must not be null
 */
public record RecordSeriesRequest(
        @Positive(message = "repetitionsToDo must be positive")
        int repetitionsToDo,

        @NotNull(message = "intensity must not be null")
        @Min(value = 1, message = "intensity must be at least 1 (RPE scale)")
        @Max(value = 10, message = "intensity must be at most 10 (RPE scale)")
        Integer intensity,

        @DecimalMin(value = "0.0", inclusive = false, message = "weight must be greater than 0")
        BigDecimal weight,

        @NotNull(message = "restTime must not be null")
        RestTime restTime
) {}


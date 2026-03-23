package com.n1b3lung0.gymrat.application.dto;

import com.n1b3lung0.gymrat.domain.model.ExerciseSeriesId;
import com.n1b3lung0.gymrat.domain.model.RestTime;

import java.math.BigDecimal;

/**
 * Command to record a new series set within an exercise-series.
 * The {@code serialNumber} is auto-computed from the existing count.
 *
 * @param exerciseSeriesId parent exercise-series
 * @param repetitionsToDo  planned repetitions; must be positive
 * @param intensity        RPE value 1–10
 * @param weight           load in kg; {@code null} for bodyweight sets
 * @param restTime         rest period after this set
 */
public record RecordSeriesCommand(
        ExerciseSeriesId exerciseSeriesId,
        int repetitionsToDo,
        int intensity,
        BigDecimal weight,
        RestTime restTime
) {}


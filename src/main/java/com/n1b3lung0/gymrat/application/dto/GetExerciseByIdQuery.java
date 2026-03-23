package com.n1b3lung0.gymrat.application.dto;

import com.n1b3lung0.gymrat.domain.model.ExerciseId;

/**
 * Query to retrieve the full detail of a single exercise.
 *
 * @param id identifier of the exercise to retrieve
 */
public record GetExerciseByIdQuery(ExerciseId id) {}


package com.n1b3lung0.gymrat.application.port.input.query;

import com.n1b3lung0.gymrat.application.dto.ExerciseSeriesSummaryView;
import com.n1b3lung0.gymrat.application.dto.ListExerciseSeriesByWorkoutQuery;

import java.util.List;

/** Input port — use case for listing all exercise-series within a workout. */
public interface ListExerciseSeriesByWorkoutUseCase {
    List<ExerciseSeriesSummaryView> execute(ListExerciseSeriesByWorkoutQuery query);
}


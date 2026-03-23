package com.n1b3lung0.gymrat.application.command;

import com.n1b3lung0.gymrat.application.dto.AddExerciseToWorkoutCommand;
import com.n1b3lung0.gymrat.application.port.input.command.AddExerciseToWorkoutUseCase;
import com.n1b3lung0.gymrat.application.port.output.DomainEventPublisherPort;
import com.n1b3lung0.gymrat.domain.exception.ExerciseNotFoundException;
import com.n1b3lung0.gymrat.domain.exception.WorkoutNotFoundException;
import com.n1b3lung0.gymrat.domain.model.ExerciseSeries;
import com.n1b3lung0.gymrat.domain.model.ExerciseSeriesId;
import com.n1b3lung0.gymrat.domain.repository.ExerciseRepositoryPort;
import com.n1b3lung0.gymrat.domain.repository.ExerciseSeriesRepositoryPort;
import com.n1b3lung0.gymrat.domain.repository.WorkoutRepositoryPort;

import java.util.Objects;

/**
 * Handles the {@link AddExerciseToWorkoutCommand} use case.
 *
 * <ol>
 *   <li>Verifies the workout exists.
 *   <li>Verifies the exercise exists.
 *   <li>Creates a new {@link ExerciseSeries} linking workout + exercise.
 *   <li>Registers the exercise-series reference on both aggregates.
 *   <li>Persists all three aggregates.
 * </ol>
 */
public class AddExerciseToWorkoutHandler implements AddExerciseToWorkoutUseCase {

    private final WorkoutRepositoryPort          workoutRepository;
    private final ExerciseRepositoryPort         exerciseRepository;
    private final ExerciseSeriesRepositoryPort   exerciseSeriesRepository;
    private final DomainEventPublisherPort       eventPublisher;

    public AddExerciseToWorkoutHandler(
            WorkoutRepositoryPort workoutRepository,
            ExerciseRepositoryPort exerciseRepository,
            ExerciseSeriesRepositoryPort exerciseSeriesRepository,
            DomainEventPublisherPort eventPublisher) {
        this.workoutRepository        = Objects.requireNonNull(workoutRepository);
        this.exerciseRepository       = Objects.requireNonNull(exerciseRepository);
        this.exerciseSeriesRepository = Objects.requireNonNull(exerciseSeriesRepository);
        this.eventPublisher           = Objects.requireNonNull(eventPublisher);
    }

    @Override
    public ExerciseSeriesId execute(AddExerciseToWorkoutCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        // 1. Verify workout exists
        var workout = workoutRepository.findById(command.workoutId())
                .orElseThrow(() -> new WorkoutNotFoundException(command.workoutId()));

        // 2. Verify exercise exists
        var exercise = exerciseRepository.findById(command.exerciseId())
                .orElseThrow(() -> new ExerciseNotFoundException(command.exerciseId()));

        // 3. Create ExerciseSeries aggregate
        var exerciseSeries = ExerciseSeries.create(workout.getId(), exercise.getId());

        // 4. Register cross-aggregate references
        workout.addExerciseSeries(exerciseSeries.getId());
        exercise.addExerciseSeries(exerciseSeries.getId());

        // 5. Persist all three aggregates
        exerciseSeriesRepository.save(exerciseSeries);
        workoutRepository.save(workout);
        exerciseRepository.save(exercise);

        // 6. Publish events
        exerciseSeries.pullDomainEvents().forEach(eventPublisher::publish);
        workout.pullDomainEvents().forEach(eventPublisher::publish);
        exercise.pullDomainEvents().forEach(eventPublisher::publish);

        return exerciseSeries.getId();
    }
}


package com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence;

import com.n1b3lung0.gymrat.application.dto.ExerciseSeriesDetailView;
import com.n1b3lung0.gymrat.application.dto.ExerciseSeriesSummaryView;
import com.n1b3lung0.gymrat.application.port.output.ExerciseSeriesQueryPort;
import com.n1b3lung0.gymrat.domain.model.ExerciseSeries;
import com.n1b3lung0.gymrat.domain.model.ExerciseSeriesId;
import com.n1b3lung0.gymrat.domain.model.WorkoutId;
import com.n1b3lung0.gymrat.domain.repository.ExerciseSeriesRepositoryPort;
import com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence.entity.ExerciseSeriesEntity;
import com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence.mapper.ExerciseSeriesPersistenceMapper;
import com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence.repository.SpringExerciseRepository;
import com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence.repository.SpringExerciseSeriesRepository;
import com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence.repository.SpringWorkoutRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * JPA adapter that implements both {@link ExerciseSeriesRepositoryPort} (command side)
 * and {@link ExerciseSeriesQueryPort} (CQRS query side).
 *
 * <p>On save, the {@code workout} and {@code exercise} FK references are resolved
 * from their respective repositories using the IDs carried on the domain aggregate.
 */
@Component
public class ExerciseSeriesJpaAdapter implements ExerciseSeriesRepositoryPort, ExerciseSeriesQueryPort {

    private final SpringExerciseSeriesRepository exerciseSeriesRepository;
    private final SpringWorkoutRepository        workoutRepository;
    private final SpringExerciseRepository       exerciseRepository;
    private final ExerciseSeriesPersistenceMapper mapper;

    public ExerciseSeriesJpaAdapter(
            SpringExerciseSeriesRepository exerciseSeriesRepository,
            SpringWorkoutRepository workoutRepository,
            SpringExerciseRepository exerciseRepository,
            ExerciseSeriesPersistenceMapper mapper) {
        this.exerciseSeriesRepository = exerciseSeriesRepository;
        this.workoutRepository        = workoutRepository;
        this.exerciseRepository       = exerciseRepository;
        this.mapper                   = mapper;
    }

    // -------------------------------------------------------------------------
    // ExerciseSeriesRepositoryPort — command side
    // -------------------------------------------------------------------------

    @Override
    public ExerciseSeries save(ExerciseSeries exerciseSeries) {
        var entity = mapper.toEntity(exerciseSeries);

        // Resolve FK references that the mapper left null
        workoutRepository.findById(exerciseSeries.getWorkoutId().value())
                .ifPresent(entity::setWorkout);
        exerciseRepository.findById(exerciseSeries.getExerciseId().value())
                .ifPresent(entity::setExercise);

        return mapper.toDomain(exerciseSeriesRepository.save(entity));
    }

    @Override
    public Optional<ExerciseSeries> findById(ExerciseSeriesId id) {
        return exerciseSeriesRepository.findWithAllById(id.value())
                .map(mapper::toDomain);
    }

    @Override
    public List<ExerciseSeries> findAllByWorkoutId(WorkoutId workoutId) {
        return exerciseSeriesRepository.findAllByWorkout_Id(workoutId.value())
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(ExerciseSeriesId id) {
        exerciseSeriesRepository.deleteById(id.value());
    }

    // -------------------------------------------------------------------------
    // ExerciseSeriesQueryPort — CQRS query side
    // -------------------------------------------------------------------------

    @Override
    public Optional<ExerciseSeriesDetailView> findDetailById(ExerciseSeriesId id) {
        return exerciseSeriesRepository.findWithAllById(id.value())
                .map(mapper::toDetailView);
    }

    @Override
    public List<ExerciseSeriesSummaryView> findAllSummariesByWorkoutId(WorkoutId workoutId) {
        return exerciseSeriesRepository.findAllByWorkout_Id(workoutId.value())
                .stream()
                .map(mapper::toSummaryView)
                .toList();
    }
}



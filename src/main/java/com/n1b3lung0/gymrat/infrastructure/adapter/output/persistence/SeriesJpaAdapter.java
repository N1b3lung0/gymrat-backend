package com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence;

import com.n1b3lung0.gymrat.application.dto.SeriesDetailView;
import com.n1b3lung0.gymrat.application.dto.SeriesSummaryView;
import com.n1b3lung0.gymrat.application.port.output.SeriesQueryPort;
import com.n1b3lung0.gymrat.domain.model.ExerciseSeriesId;
import com.n1b3lung0.gymrat.domain.model.Series;
import com.n1b3lung0.gymrat.domain.model.SeriesId;
import com.n1b3lung0.gymrat.domain.repository.SeriesRepositoryPort;
import com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence.mapper.SeriesPersistenceMapper;
import com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence.repository.SpringExerciseSeriesRepository;
import com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence.repository.SpringSeriesRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * JPA adapter that implements both {@link SeriesRepositoryPort} (command side)
 * and {@link SeriesQueryPort} (CQRS query side).
 *
 * <p>On save, the {@code exerciseSeries} FK reference is resolved from its
 * repository using the ID carried on the domain aggregate.
 */
@Component
public class SeriesJpaAdapter implements SeriesRepositoryPort, SeriesQueryPort {

    private final SpringSeriesRepository          seriesRepository;
    private final SpringExerciseSeriesRepository  exerciseSeriesRepository;
    private final SeriesPersistenceMapper         mapper;

    public SeriesJpaAdapter(
            SpringSeriesRepository seriesRepository,
            SpringExerciseSeriesRepository exerciseSeriesRepository,
            SeriesPersistenceMapper mapper) {
        this.seriesRepository         = seriesRepository;
        this.exerciseSeriesRepository = exerciseSeriesRepository;
        this.mapper                   = mapper;
    }

    // -------------------------------------------------------------------------
    // SeriesRepositoryPort — command side
    // -------------------------------------------------------------------------

    @Override
    public Series save(Series series) {
        var entity = mapper.toEntity(series);

        // Resolve the exerciseSeries FK reference that the mapper left null
        exerciseSeriesRepository.findById(series.getExerciseSeriesId().value())
                .ifPresent(entity::setExerciseSeries);

        return mapper.toDomain(seriesRepository.save(entity));
    }

    @Override
    public Optional<Series> findById(SeriesId id) {
        return seriesRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public List<Series> findAllByExerciseSeriesId(ExerciseSeriesId exerciseSeriesId) {
        return seriesRepository
                .findAllByExerciseSeries_IdOrderBySerialNumberAsc(exerciseSeriesId.value())
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(SeriesId id) {
        seriesRepository.deleteById(id.value());
    }

    @Override
    public long countByExerciseSeriesId(ExerciseSeriesId exerciseSeriesId) {
        return seriesRepository.countByExerciseSeries_Id(exerciseSeriesId.value());
    }

    // -------------------------------------------------------------------------
    // SeriesQueryPort — CQRS query side
    // -------------------------------------------------------------------------

    @Override
    public Optional<SeriesDetailView> findDetailById(SeriesId id) {
        return seriesRepository.findById(id.value()).map(mapper::toDetailView);
    }

    @Override
    public List<SeriesSummaryView> findAllSummariesByExerciseSeriesId(ExerciseSeriesId exerciseSeriesId) {
        return seriesRepository
                .findAllByExerciseSeries_IdOrderBySerialNumberAsc(exerciseSeriesId.value())
                .stream()
                .map(mapper::toSummaryView)
                .toList();
    }
}


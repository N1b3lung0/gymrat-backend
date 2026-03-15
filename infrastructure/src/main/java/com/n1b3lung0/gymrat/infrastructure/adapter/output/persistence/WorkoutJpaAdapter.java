package com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence;

import com.n1b3lung0.gymrat.application.dto.WorkoutDetailView;
import com.n1b3lung0.gymrat.application.dto.WorkoutSummaryView;
import com.n1b3lung0.gymrat.application.port.output.WorkoutQueryPort;
import com.n1b3lung0.gymrat.domain.model.Workout;
import com.n1b3lung0.gymrat.domain.model.WorkoutId;
import com.n1b3lung0.gymrat.domain.repository.PageRequest;
import com.n1b3lung0.gymrat.domain.repository.PageResult;
import com.n1b3lung0.gymrat.domain.repository.WorkoutRepositoryPort;
import com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence.mapper.WorkoutPersistenceMapper;
import com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence.repository.SpringWorkoutRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * JPA adapter that implements both {@link WorkoutRepositoryPort} (command side)
 * and {@link WorkoutQueryPort} (CQRS query side).
 *
 * <p>Soft-delete is handled by the {@code @SQLDelete} annotation on the entity.
 */
@Component
public class WorkoutJpaAdapter implements WorkoutRepositoryPort, WorkoutQueryPort {

    private final SpringWorkoutRepository workoutRepository;
    private final WorkoutPersistenceMapper mapper;

    public WorkoutJpaAdapter(
            SpringWorkoutRepository workoutRepository,
            WorkoutPersistenceMapper mapper) {
        this.workoutRepository = workoutRepository;
        this.mapper            = mapper;
    }

    // -------------------------------------------------------------------------
    // WorkoutRepositoryPort — command side
    // -------------------------------------------------------------------------

    @Override
    public Workout save(Workout workout) {
        return mapper.toDomain(workoutRepository.save(mapper.toEntity(workout)));
    }

    @Override
    public Optional<Workout> findById(WorkoutId id) {
        return workoutRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public PageResult<Workout> findAll(PageRequest pageRequest) {
        var page = workoutRepository.findAll(toPageable(pageRequest));
        return new PageResult<>(
                page.getContent().stream().map(mapper::toDomain).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        );
    }

    @Override
    public void deleteById(WorkoutId id) {
        workoutRepository.deleteById(id.value());
    }

    // -------------------------------------------------------------------------
    // WorkoutQueryPort — CQRS query side
    // -------------------------------------------------------------------------

    @Override
    public Optional<WorkoutDetailView> findDetailById(WorkoutId id) {
        return workoutRepository.findById(id.value()).map(mapper::toDetailView);
    }

    @Override
    public PageResult<WorkoutSummaryView> findAllSummaries(PageRequest pageRequest) {
        var page = workoutRepository.findAll(toPageable(pageRequest));
        return new PageResult<>(
                page.getContent().stream().map(mapper::toSummaryView).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        );
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private org.springframework.data.domain.Pageable toPageable(PageRequest req) {
        if (req.sortBy() == null) {
            return org.springframework.data.domain.PageRequest.of(req.page(), req.size());
        }
        var direction = req.ascending() ? Sort.Direction.ASC : Sort.Direction.DESC;
        return org.springframework.data.domain.PageRequest.of(
                req.page(), req.size(), Sort.by(direction, req.sortBy()));
    }
}



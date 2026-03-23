package com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence;

import com.n1b3lung0.gymrat.application.dto.ExerciseDetailView;
import com.n1b3lung0.gymrat.application.dto.ExerciseSummaryView;
import com.n1b3lung0.gymrat.application.port.output.ExerciseQueryPort;
import com.n1b3lung0.gymrat.domain.model.Exercise;
import com.n1b3lung0.gymrat.domain.model.ExerciseId;
import com.n1b3lung0.gymrat.domain.repository.ExerciseRepositoryPort;
import com.n1b3lung0.gymrat.domain.repository.PageRequest;
import com.n1b3lung0.gymrat.domain.repository.PageResult;
import com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence.entity.ExerciseEntity;
import com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence.entity.MediaEntity;
import com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence.mapper.ExercisePersistenceMapper;
import com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence.repository.SpringExerciseRepository;
import com.n1b3lung0.gymrat.infrastructure.adapter.output.persistence.repository.SpringMediaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * JPA adapter that implements both {@link ExerciseRepositoryPort} (command side)
 * and {@link ExerciseQueryPort} (CQRS query side).
 *
 * <p>Soft-delete is handled by the {@code @SQLDelete} annotation on
 * {@link ExerciseEntity} — calling {@code deleteById} issues an UPDATE, not a DELETE.
 */
@Component
public class ExerciseJpaAdapter implements ExerciseRepositoryPort, ExerciseQueryPort {

    private final SpringExerciseRepository exerciseRepository;
    private final SpringMediaRepository    mediaRepository;
    private final ExercisePersistenceMapper mapper;

    public ExerciseJpaAdapter(
            SpringExerciseRepository exerciseRepository,
            SpringMediaRepository mediaRepository,
            ExercisePersistenceMapper mapper) {
        this.exerciseRepository = exerciseRepository;
        this.mediaRepository    = mediaRepository;
        this.mapper             = mapper;
    }

    // -------------------------------------------------------------------------
    // ExerciseRepositoryPort — command side
    // -------------------------------------------------------------------------

    @Override
    public Exercise save(Exercise exercise) {
        var entity = mapper.toEntity(exercise);

        // Resolve / persist media entities before saving exercise
        entity.setImage(resolveMedia(exercise.getImage() != null ? exercise.getImage().url() : null,
                entity.getImage()));
        entity.setVideo(resolveMedia(exercise.getVideo() != null ? exercise.getVideo().url() : null,
                entity.getVideo()));

        return mapper.toDomain(exerciseRepository.save(entity));
    }

    @Override
    public Optional<Exercise> findById(ExerciseId id) {
        return exerciseRepository.findWithAllById(id.value())
                .map(mapper::toDomain);
    }

    @Override
    public PageResult<Exercise> findAll(PageRequest pageRequest) {
        var pageable = toPageable(pageRequest);
        var page = exerciseRepository.findAll(pageable);
        return new PageResult<>(
                page.getContent().stream().map(mapper::toDomain).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        );
    }

    @Override
    public void deleteById(ExerciseId id) {
        // @SQLDelete on ExerciseEntity converts this to a soft-delete UPDATE
        exerciseRepository.deleteById(id.value());
    }

    @Override
    public boolean existsByName(String name) {
        return exerciseRepository.existsByName(name);
    }

    // -------------------------------------------------------------------------
    // ExerciseQueryPort — CQRS query side
    // -------------------------------------------------------------------------

    @Override
    public Optional<ExerciseDetailView> findDetailById(ExerciseId id) {
        return exerciseRepository.findWithAllById(id.value())
                .map(mapper::toDetailView);
    }

    @Override
    public PageResult<ExerciseSummaryView> findAllSummaries(PageRequest pageRequest) {
        var pageable = toPageable(pageRequest);
        var page = exerciseRepository.findAll(pageable);
        return new PageResult<>(
                page.getContent().stream().map(mapper::toSummaryView).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        );
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Resolves a {@link MediaEntity} reference: if a media asset with the given URL
     * already exists in the database it is reused; otherwise the provided new entity
     * (with a freshly generated ID) is saved first.
     */
    private MediaEntity resolveMedia(String url, MediaEntity newEntity) {
        if (url == null || newEntity == null) return null;
        return mediaRepository.findByUrl(url)
                .orElseGet(() -> {
                    if (newEntity.getId() == null) newEntity.setId(UUID.randomUUID());
                    return mediaRepository.save(newEntity);
                });
    }

    /** Converts the domain {@link PageRequest} to a Spring Data {@link org.springframework.data.domain.Pageable}. */
    private org.springframework.data.domain.Pageable toPageable(PageRequest req) {
        if (req.sortBy() == null) {
            return org.springframework.data.domain.PageRequest.of(req.page(), req.size());
        }
        var direction = req.ascending() ? Sort.Direction.ASC : Sort.Direction.DESC;
        return org.springframework.data.domain.PageRequest.of(
                req.page(), req.size(), Sort.by(direction, req.sortBy()));
    }
}


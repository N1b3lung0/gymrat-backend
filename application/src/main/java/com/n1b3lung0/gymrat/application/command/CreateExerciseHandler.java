package com.n1b3lung0.gymrat.application.command;

import com.n1b3lung0.gymrat.application.dto.CreateExerciseCommand;
import com.n1b3lung0.gymrat.application.port.input.command.CreateExerciseUseCase;
import com.n1b3lung0.gymrat.application.port.output.DomainEventPublisherPort;
import com.n1b3lung0.gymrat.application.port.output.MetricsPort;
import com.n1b3lung0.gymrat.domain.exception.DuplicateExerciseNameException;
import com.n1b3lung0.gymrat.domain.model.Exercise;
import com.n1b3lung0.gymrat.domain.model.ExerciseId;
import com.n1b3lung0.gymrat.domain.model.Media;
import com.n1b3lung0.gymrat.domain.repository.ExerciseRepositoryPort;
import com.n1b3lung0.gymrat.domain.repository.MediaRepositoryPort;

import java.util.Objects;

/**
 * Handles the {@link CreateExerciseCommand} use case.
 *
 * <ol>
 *   <li>Guards against duplicate exercise names.
 *   <li>Resolves or persists {@link Media} assets (deduplication by URL).
 *   <li>Creates the {@link Exercise} aggregate via its factory method.
 *   <li>Persists the aggregate.
 *   <li>Publishes accumulated domain events.
 *   <li>Increments the {@code exercises.created.total} metric counter.
 * </ol>
 */
public class CreateExerciseHandler implements CreateExerciseUseCase {

    static final String EXERCISES_CREATED_METRIC = "exercises.created.total";

    private final ExerciseRepositoryPort exerciseRepository;
    private final MediaRepositoryPort mediaRepository;
    private final DomainEventPublisherPort eventPublisher;
    private final MetricsPort metrics;

    public CreateExerciseHandler(
            ExerciseRepositoryPort exerciseRepository,
            MediaRepositoryPort mediaRepository,
            DomainEventPublisherPort eventPublisher,
            MetricsPort metrics) {
        this.exerciseRepository = Objects.requireNonNull(exerciseRepository);
        this.mediaRepository    = Objects.requireNonNull(mediaRepository);
        this.eventPublisher     = Objects.requireNonNull(eventPublisher);
        this.metrics            = Objects.requireNonNull(metrics);
    }

    @Override
    public ExerciseId execute(CreateExerciseCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        // 1. Duplicate name guard
        if (exerciseRepository.existsByName(command.name())) {
            throw new DuplicateExerciseNameException(command.name());
        }

        // 2. Resolve / persist media assets (deduplication by URL)
        var image = resolveMedia(command.imageUrl(), command.imageName(), command.imageDescription());
        var video = resolveMedia(command.videoUrl(), command.videoName(), command.videoDescription());

        // 3. Create aggregate
        var exercise = Exercise.builder()
                .name(command.name())
                .description(command.description())
                .level(command.level())
                .routines(command.routines())
                .primaryMuscle(command.primaryMuscle())
                .secondaryMuscles(command.secondaryMuscles())
                .image(image)
                .video(video)
                .build();

        // 4. Persist
        exerciseRepository.save(exercise);

        // 5. Publish domain events (always after save)
        exercise.pullDomainEvents().forEach(eventPublisher::publish);

        // 6. Metrics
        metrics.increment(EXERCISES_CREATED_METRIC);

        return exercise.getId();
    }

    /**
     * Returns an existing {@link Media} asset if the URL is already known,
     * or saves a new one otherwise. Returns {@code null} if {@code url} is blank.
     */
    private Media resolveMedia(String url, String name, String description) {
        if (url == null || url.isBlank()) return null;
        return mediaRepository.findByUrl(url)
                .orElseGet(() -> mediaRepository.save(Media.of(name, description, url)));
    }
}


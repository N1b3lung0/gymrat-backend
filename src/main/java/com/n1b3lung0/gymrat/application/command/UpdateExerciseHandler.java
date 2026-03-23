package com.n1b3lung0.gymrat.application.command;

import com.n1b3lung0.gymrat.application.dto.UpdateExerciseCommand;
import com.n1b3lung0.gymrat.application.port.input.command.UpdateExerciseUseCase;
import com.n1b3lung0.gymrat.application.port.output.DomainEventPublisherPort;
import com.n1b3lung0.gymrat.domain.exception.DuplicateExerciseNameException;
import com.n1b3lung0.gymrat.domain.exception.ExerciseNotFoundException;
import com.n1b3lung0.gymrat.domain.model.Media;
import com.n1b3lung0.gymrat.domain.repository.ExerciseRepositoryPort;
import com.n1b3lung0.gymrat.domain.repository.MediaRepositoryPort;

import java.util.Objects;

/**
 * Handles the {@link UpdateExerciseCommand} use case.
 *
 * <ol>
 *   <li>Loads the existing {@link com.n1b3lung0.gymrat.domain.model.Exercise} or throws
 *       {@link ExerciseNotFoundException}.
 *   <li>Guards against duplicate names (only when the name actually changes).
 *   <li>Resolves or persists {@link Media} assets (deduplication by URL).
 *   <li>Calls {@code exercise.update(...)} to apply changes.
 *   <li>Persists the aggregate.
 *   <li>Publishes accumulated domain events.
 * </ol>
 */
public class UpdateExerciseHandler implements UpdateExerciseUseCase {

    private final ExerciseRepositoryPort exerciseRepository;
    private final MediaRepositoryPort mediaRepository;
    private final DomainEventPublisherPort eventPublisher;

    public UpdateExerciseHandler(
            ExerciseRepositoryPort exerciseRepository,
            MediaRepositoryPort mediaRepository,
            DomainEventPublisherPort eventPublisher) {
        this.exerciseRepository = Objects.requireNonNull(exerciseRepository);
        this.mediaRepository    = Objects.requireNonNull(mediaRepository);
        this.eventPublisher     = Objects.requireNonNull(eventPublisher);
    }

    @Override
    public void execute(UpdateExerciseCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        // 1. Load aggregate
        var exercise = exerciseRepository.findById(command.id())
                .orElseThrow(() -> new ExerciseNotFoundException(command.id()));

        // 2. Duplicate name guard — only when name actually changes
        if (!exercise.getName().equals(command.name())
                && exerciseRepository.existsByName(command.name())) {
            throw new DuplicateExerciseNameException(command.name());
        }

        // 3. Resolve / persist media assets
        var image = resolveMedia(command.imageUrl(), command.imageName(), command.imageDescription());
        var video = resolveMedia(command.videoUrl(), command.videoName(), command.videoDescription());

        // 4. Apply domain update
        exercise.update(
                command.name(),
                command.description(),
                command.level(),
                command.routines(),
                command.primaryMuscle(),
                command.secondaryMuscles(),
                image,
                video
        );

        // 5. Persist
        exerciseRepository.save(exercise);

        // 6. Publish domain events (always after save)
        exercise.pullDomainEvents().forEach(eventPublisher::publish);
    }

    private Media resolveMedia(String url, String name, String description) {
        if (url == null || url.isBlank()) return null;
        return mediaRepository.findByUrl(url)
                .orElseGet(() -> mediaRepository.save(Media.of(name, description, url)));
    }
}


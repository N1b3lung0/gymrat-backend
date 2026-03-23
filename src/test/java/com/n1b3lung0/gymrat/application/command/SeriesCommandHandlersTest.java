package com.n1b3lung0.gymrat.application.command;

import com.n1b3lung0.gymrat.application.dto.DeleteSeriesCommand;
import com.n1b3lung0.gymrat.application.dto.RecordSeriesCommand;
import com.n1b3lung0.gymrat.application.dto.UpdateSeriesCommand;
import com.n1b3lung0.gymrat.application.port.output.DomainEventPublisherPort;
import com.n1b3lung0.gymrat.application.port.output.MetricsPort;
import com.n1b3lung0.gymrat.domain.event.SeriesCreated;
import com.n1b3lung0.gymrat.domain.exception.ExerciseSeriesNotFoundException;
import com.n1b3lung0.gymrat.domain.exception.SeriesNotFoundException;
import com.n1b3lung0.gymrat.domain.model.ExerciseSeries;
import com.n1b3lung0.gymrat.domain.model.ExerciseSeriesId;
import com.n1b3lung0.gymrat.domain.model.ExerciseId;
import com.n1b3lung0.gymrat.domain.model.RestTime;
import com.n1b3lung0.gymrat.domain.model.Series;
import com.n1b3lung0.gymrat.domain.model.SeriesId;
import com.n1b3lung0.gymrat.domain.model.WorkoutId;
import com.n1b3lung0.gymrat.domain.repository.ExerciseSeriesRepositoryPort;
import com.n1b3lung0.gymrat.domain.repository.SeriesRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Series Command Handlers")
class SeriesCommandHandlersTest {

    @Mock SeriesRepositoryPort         seriesRepository;
    @Mock ExerciseSeriesRepositoryPort exerciseSeriesRepository;
    @Mock DomainEventPublisherPort     eventPublisher;
    @Mock MetricsPort                  metrics;

    RecordSeriesHandler recordHandler;
    UpdateSeriesHandler updateHandler;
    DeleteSeriesHandler deleteHandler;

    @BeforeEach
    void setUp() {
        recordHandler = new RecordSeriesHandler(seriesRepository, exerciseSeriesRepository, eventPublisher, metrics);
        updateHandler = new UpdateSeriesHandler(seriesRepository, eventPublisher);
        deleteHandler = new DeleteSeriesHandler(seriesRepository);
    }

    private static ExerciseSeries exerciseSeries() {
        return ExerciseSeries.create(WorkoutId.generate(), ExerciseId.generate());
    }

    private static RecordSeriesCommand recordCommand(ExerciseSeriesId esId) {
        return new RecordSeriesCommand(esId, 10, 7, BigDecimal.valueOf(80), RestTime.SIXTY);
    }

    // -------------------------------------------------------------------------
    // RecordSeriesHandler
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("RecordSeriesHandler")
    class Record {

        @Test
        @DisplayName("should return a non-null SeriesId")
        void shouldReturnSeriesId() {
            var es = exerciseSeries();
            when(exerciseSeriesRepository.findById(es.getId())).thenReturn(Optional.of(es));
            when(seriesRepository.countByExerciseSeriesId(es.getId())).thenReturn(0L);
            when(seriesRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(exerciseSeriesRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            var id = recordHandler.execute(recordCommand(es.getId()));

            assertNotNull(id);
            assertNotNull(id.value());
        }

        @Test
        @DisplayName("should compute serialNumber as count + 1")
        void shouldComputeSerialNumber() {
            var es = exerciseSeries();
            when(exerciseSeriesRepository.findById(es.getId())).thenReturn(Optional.of(es));
            when(seriesRepository.countByExerciseSeriesId(es.getId())).thenReturn(2L);
            when(seriesRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(exerciseSeriesRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            recordHandler.execute(recordCommand(es.getId()));

            var captor = ArgumentCaptor.forClass(Series.class);
            verify(seriesRepository).save(captor.capture());
            assertEquals(3, captor.getValue().getSerialNumber());
        }

        @Test
        @DisplayName("should publish SeriesCreated event after save")
        void shouldPublishSeriesCreatedEvent() {
            var es = exerciseSeries();
            when(exerciseSeriesRepository.findById(es.getId())).thenReturn(Optional.of(es));
            when(seriesRepository.countByExerciseSeriesId(es.getId())).thenReturn(0L);
            when(seriesRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(exerciseSeriesRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            recordHandler.execute(recordCommand(es.getId()));

            var captor = ArgumentCaptor.forClass(Object.class);
            verify(eventPublisher, atLeastOnce()).publish(captor.capture());
            assertTrue(captor.getAllValues().stream()
                    .anyMatch(e -> e instanceof SeriesCreated));
        }

        @Test
        @DisplayName("should throw ExerciseSeriesNotFoundException when parent not found")
        void shouldThrow_whenExerciseSeriesNotFound() {
            var esId = ExerciseSeriesId.generate();
            when(exerciseSeriesRepository.findById(esId)).thenReturn(Optional.empty());

            assertThrows(ExerciseSeriesNotFoundException.class,
                    () -> recordHandler.execute(recordCommand(esId)));

            verify(seriesRepository, never()).save(any());
        }
    }

    // -------------------------------------------------------------------------
    // UpdateSeriesHandler
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("UpdateSeriesHandler")
    class Update {

        @Test
        @DisplayName("should persist updated series")
        void shouldSaveUpdatedSeries() {
            var esId   = ExerciseSeriesId.generate();
            var series = Series.create(1, 10, 7, BigDecimal.valueOf(80), RestTime.SIXTY, esId);
            series.pullDomainEvents();
            when(seriesRepository.findById(series.getId())).thenReturn(Optional.of(series));
            when(seriesRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            updateHandler.execute(new UpdateSeriesCommand(
                    series.getId(), 12, null, 8,
                    BigDecimal.valueOf(85), null, null, RestTime.NINETY));

            verify(seriesRepository).save(series);
            assertEquals(12, series.getRepetitionsToDo());
            assertEquals(8, series.getIntensity());
        }

        @Test
        @DisplayName("should throw SeriesNotFoundException when series does not exist")
        void shouldThrow_whenNotFound() {
            var id = SeriesId.generate();
            when(seriesRepository.findById(id)).thenReturn(Optional.empty());

            assertThrows(SeriesNotFoundException.class,
                    () -> updateHandler.execute(new UpdateSeriesCommand(
                            id, 10, null, 7, null, null, null, RestTime.SIXTY)));

            verify(seriesRepository, never()).save(any());
        }
    }

    // -------------------------------------------------------------------------
    // DeleteSeriesHandler
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("DeleteSeriesHandler")
    class Delete {

        @Test
        @DisplayName("should call deleteById on repository")
        void shouldCallDeleteById() {
            var esId   = ExerciseSeriesId.generate();
            var series = Series.create(1, 10, 7, BigDecimal.valueOf(80), RestTime.SIXTY, esId);
            when(seriesRepository.findById(series.getId())).thenReturn(Optional.of(series));

            deleteHandler.execute(new DeleteSeriesCommand(series.getId()));

            verify(seriesRepository).deleteById(series.getId());
        }

        @Test
        @DisplayName("should throw SeriesNotFoundException when not found")
        void shouldThrow_whenNotFound() {
            var id = SeriesId.generate();
            when(seriesRepository.findById(id)).thenReturn(Optional.empty());

            assertThrows(SeriesNotFoundException.class,
                    () -> deleteHandler.execute(new DeleteSeriesCommand(id)));

            verify(seriesRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("should throw NullPointerException when command is null")
        void shouldThrow_whenCommandIsNull() {
            assertThrows(NullPointerException.class, () -> deleteHandler.execute(null));
        }
    }
}


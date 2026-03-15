package com.n1b3lung0.gymrat.application.query;

import com.n1b3lung0.gymrat.application.dto.GetSeriesByIdQuery;
import com.n1b3lung0.gymrat.application.dto.ListSeriesByExerciseSeriesQuery;
import com.n1b3lung0.gymrat.application.dto.SeriesDetailView;
import com.n1b3lung0.gymrat.application.dto.SeriesSummaryView;
import com.n1b3lung0.gymrat.application.port.output.SeriesQueryPort;
import com.n1b3lung0.gymrat.domain.exception.SeriesNotFoundException;
import com.n1b3lung0.gymrat.domain.model.ExerciseSeriesId;
import com.n1b3lung0.gymrat.domain.model.RestTime;
import com.n1b3lung0.gymrat.domain.model.SeriesId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Series Query Handlers")
class SeriesQueryHandlersTest {

    @Mock SeriesQueryPort seriesQueryPort;

    GetSeriesByIdHandler                getByIdHandler;
    ListSeriesByExerciseSeriesHandler   listHandler;

    @BeforeEach
    void setUp() {
        getByIdHandler = new GetSeriesByIdHandler(seriesQueryPort);
        listHandler    = new ListSeriesByExerciseSeriesHandler(seriesQueryPort);
    }

    // -------------------------------------------------------------------------
    // GetSeriesByIdHandler
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GetSeriesByIdHandler")
    class GetById {

        @Test
        @DisplayName("should return SeriesDetailView when found")
        void shouldReturnDetailView_whenFound() {
            var id   = SeriesId.generate();
            var view = new SeriesDetailView(id.value(), 1, 10, null, 7,
                    BigDecimal.valueOf(80), null, null, RestTime.SIXTY, UUID.randomUUID());
            when(seriesQueryPort.findDetailById(id)).thenReturn(Optional.of(view));

            var result = getByIdHandler.execute(new GetSeriesByIdQuery(id));

            assertNotNull(result);
            assertEquals(id.value(), result.id());
            assertEquals(1, result.serialNumber());
        }

        @Test
        @DisplayName("should throw SeriesNotFoundException when not found")
        void shouldThrow_whenNotFound() {
            var id = SeriesId.generate();
            when(seriesQueryPort.findDetailById(id)).thenReturn(Optional.empty());

            assertThrows(SeriesNotFoundException.class,
                    () -> getByIdHandler.execute(new GetSeriesByIdQuery(id)));
        }

        @Test
        @DisplayName("should throw NullPointerException when query is null")
        void shouldThrow_whenQueryIsNull() {
            assertThrows(NullPointerException.class, () -> getByIdHandler.execute(null));
        }
    }

    // -------------------------------------------------------------------------
    // ListSeriesByExerciseSeriesHandler
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("ListSeriesByExerciseSeriesHandler")
    class ListByExerciseSeries {

        @Test
        @DisplayName("should return ordered list of summaries")
        void shouldReturnSummaries() {
            var esId      = ExerciseSeriesId.generate();
            var summaries = List.of(
                    new SeriesSummaryView(UUID.randomUUID(), 1, 10, 7, BigDecimal.valueOf(80), RestTime.SIXTY),
                    new SeriesSummaryView(UUID.randomUUID(), 2, 8,  8, BigDecimal.valueOf(82.5), RestTime.NINETY));
            when(seriesQueryPort.findAllSummariesByExerciseSeriesId(esId)).thenReturn(summaries);

            var result = listHandler.execute(new ListSeriesByExerciseSeriesQuery(esId));

            assertEquals(2, result.size());
            assertEquals(1, result.get(0).serialNumber());
            assertEquals(2, result.get(1).serialNumber());
        }

        @Test
        @DisplayName("should return empty list when no series exist")
        void shouldReturnEmptyList_whenNoResults() {
            var esId = ExerciseSeriesId.generate();
            when(seriesQueryPort.findAllSummariesByExerciseSeriesId(esId)).thenReturn(List.of());

            var result = listHandler.execute(new ListSeriesByExerciseSeriesQuery(esId));

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should throw NullPointerException when query is null")
        void shouldThrow_whenQueryIsNull() {
            assertThrows(NullPointerException.class, () -> listHandler.execute(null));
        }
    }
}


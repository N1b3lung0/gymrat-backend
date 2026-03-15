package com.n1b3lung0.gymrat.application.dto;

import com.n1b3lung0.gymrat.domain.model.SeriesId;

/**
 * Command to soft-delete a series set.
 *
 * @param id identifier of the series to delete
 */
public record DeleteSeriesCommand(SeriesId id) {}


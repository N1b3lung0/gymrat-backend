package com.n1b3lung0.gymrat.application.dto;

import com.n1b3lung0.gymrat.domain.model.SeriesId;

/**
 * Query to retrieve the full detail of a single series set.
 *
 * @param id the series identifier
 */
public record GetSeriesByIdQuery(SeriesId id) {}


package com.n1b3lung0.gymrat.application.dto;

import com.n1b3lung0.gymrat.domain.repository.PageRequest;

/**
 * Query to retrieve a paginated list of workout summaries.
 *
 * @param pageRequest pagination and sorting parameters
 */
public record ListWorkoutsQuery(PageRequest pageRequest) {}


package com.n1b3lung0.gymrat.domain.exception;

import com.n1b3lung0.gymrat.domain.model.SeriesId;

/**
 * Thrown when a {@code Series} cannot be found by its identifier.
 */
public class SeriesNotFoundException extends NotFoundException {

    public SeriesNotFoundException(SeriesId id) {
        super("Series not found with id: " + id);
    }
}


package com.n1b3lung0.gymrat.domain.repository;

/**
 * Domain-level pagination request, free of any framework dependency.
 *
 * <p>The infrastructure adapter converts this to a Spring Data {@code Pageable}.
 *
 * @param page zero-based page index
 * @param size number of items per page; must be positive
 * @param sortBy field name to sort by; {@code null} means no explicit ordering
 * @param ascending {@code true} for ascending order, {@code false} for descending
 */
public record PageRequest(int page, int size, String sortBy, boolean ascending) {

    public PageRequest {
        if (page < 0) throw new IllegalArgumentException("page must not be negative");
        if (size <= 0) throw new IllegalArgumentException("size must be positive");
    }

    /** Creates an unsorted page request. */
    public static PageRequest of(int page, int size) {
        return new PageRequest(page, size, null, true);
    }

    /** Creates a sorted page request. */
    public static PageRequest of(int page, int size, String sortBy, boolean ascending) {
        return new PageRequest(page, size, sortBy, ascending);
    }
}


package com.n1b3lung0.gymrat.domain.repository;

import java.util.List;

/**
 * Domain-level paginated result, free of any framework dependency.
 *
 * <p>The infrastructure adapter converts Spring Data's {@code Page<T>} into this type
 * before returning it to the application layer.
 *
 * @param <T>         type of items in the page
 * @param content     items in the current page
 * @param page        zero-based page index
 * @param size        requested page size
 * @param totalElements total number of matching items across all pages
 */
public record PageResult<T>(List<T> content, int page, int size, long totalElements) {

    public PageResult {
        content = List.copyOf(content);
    }

    /** Returns the total number of pages. */
    public int totalPages() {
        return size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
    }

    /** Returns {@code true} if this is the last page. */
    public boolean isLast() {
        return page >= totalPages() - 1;
    }

    /** Returns {@code true} if this is the first page. */
    public boolean isFirst() {
        return page == 0;
    }
}


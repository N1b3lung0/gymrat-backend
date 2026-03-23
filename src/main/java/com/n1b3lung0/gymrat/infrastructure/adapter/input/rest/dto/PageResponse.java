package com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Standardized paginated response envelope for all list endpoints.
 *
 * <p>Never return a raw array for paginated collections. Every {@code GET /resource}
 * endpoint that supports paging must wrap its results in this record.
 *
 * <p>Use the {@link #of(Page)} factory to build from a Spring Data {@link Page}.
 *
 * @param <T>           type of items in the current page
 * @param content       items in the current page
 * @param page          zero-based page index
 * @param size          requested page size
 * @param totalElements total number of matching items across all pages
 * @param totalPages    total number of pages
 * @param last          {@code true} if this is the last page
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {

    /**
     * Builds a {@code PageResponse} from a Spring Data {@link Page}.
     *
     * @param <T>  item type
     * @param page the Spring Data page
     * @return a populated {@code PageResponse}
     */
    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    /**
     * Builds a {@code PageResponse} from a domain {@link com.n1b3lung0.gymrat.domain.repository.PageResult}.
     *
     * <p>Used when the content has already been mapped from entity to response DTO
     * before building the envelope.
     *
     * @param <T>    item type
     * @param result the domain page result
     * @return a populated {@code PageResponse}
     */
    public static <T> PageResponse<T> of(com.n1b3lung0.gymrat.domain.repository.PageResult<T> result) {
        return new PageResponse<>(
                result.content(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                result.isLast()
        );
    }
}


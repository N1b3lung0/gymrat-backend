package com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto;

/**
 * REST response DTO for a media asset (image or video) embedded in exercise responses.
 *
 * @param name        human-readable name of the asset; may be {@code null}
 * @param description optional description; may be {@code null}
 * @param url         publicly accessible URL of the asset
 */
public record MediaResponse(String name, String description, String url) {}


package com.n1b3lung0.gymrat.application.dto;

/**
 * Read model for a media asset (image or video) embedded in exercise views.
 *
 * @param name        human-readable name of the asset; may be {@code null}
 * @param description optional description; may be {@code null}
 * @param url         publicly accessible URL of the asset
 */
public record MediaView(String name, String description, String url) {}


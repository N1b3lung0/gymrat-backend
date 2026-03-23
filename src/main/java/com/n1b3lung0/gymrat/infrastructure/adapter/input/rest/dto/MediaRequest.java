package com.n1b3lung0.gymrat.infrastructure.adapter.input.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * REST request DTO for a media asset (image or video) embedded in exercise requests.
 *
 * @param name        optional human-readable name for the asset
 * @param description optional description of what the asset shows
 * @param url         publicly accessible URL of the asset; must not be blank
 */
public record MediaRequest(
        @Size(max = 255)
        String name,

        @Size(max = 1000)
        String description,

        @NotBlank(message = "Media URL must not be blank")
        @Size(max = 2048)
        String url
) {}


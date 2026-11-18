package com.snaplink.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record CreateLinkRequest(
        @NotBlank(message = "targetUrl is required")
        @Schema(example = "https://example.com/long/path")
        String targetUrl,

        @Size(min = 3, max = 30, message = "customAlias must be 3-30 characters")
        @Schema(example = "my-cv")
        String customAlias,

        @Future(message = "expiresAt must be in the future")
        @Schema(example = "2026-12-31T23:59:59Z")
        Instant expiresAt,

        @Min(value = 1, message = "maxClicks must be at least 1")
        @Schema(example = "100")
        Integer maxClicks
) {
}
package com.snaplink.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

public record UpdateLinkRequest(
        @Schema(example = "2026-12-31T23:59:59Z")
        Instant expiresAt,

        @Schema(example = "50")
        Integer maxClicks,

        @Schema(example = "true")
        Boolean active
) {
}
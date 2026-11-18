package com.snaplink.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

public record LinkResponse(
        @Schema(example = "aB3xY9kL")
        String code,
        @Schema(example = "http://localhost:8080/aB3xY9kL")
        String shortUrl,
        @Schema(example = "https://example.com/page")
        String targetUrl,
        Instant createdAt,
        Instant expiresAt,
        Integer maxClicks,
        boolean active,
        @Schema(description = "Total recorded clicks")
        Long totalClicks
) {
}
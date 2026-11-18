package com.snaplink.dto;

import java.time.Instant;

public record LinkResponse(
        String code,
        String shortUrl,
        String targetUrl,
        Instant createdAt,
        Instant expiresAt,
        Integer maxClicks,
        boolean active,
        Long totalClicks
) {
}
package com.snaplink.dto;

import java.time.Instant;

public record UpdateLinkRequest(
        Instant expiresAt,
        Integer maxClicks,
        Boolean active
) {
}
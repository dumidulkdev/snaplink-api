package com.snaplink.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record CreateLinkRequest(
        @NotBlank(message = "targetUrl is required")
        String targetUrl,

        @Size(min = 3, max = 30, message = "customAlias must be 3-30 characters")
        String customAlias,

        @Future(message = "expiresAt must be in the future")
        Instant expiresAt,

        @Min(value = 1, message = "maxClicks must be at least 1")
        Integer maxClicks
) {
}
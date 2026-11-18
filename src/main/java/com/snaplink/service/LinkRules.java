package com.snaplink.service;

import com.snaplink.entity.Link;

import java.time.Clock;
import java.time.Instant;

public final class LinkRules {

    public enum ResolutionStatus {
        OK,
        NOT_FOUND,
        GONE
    }

    private LinkRules() {
    }

    public static ResolutionStatus resolveStatus(Link link, Clock clock) {
        if (link == null) {
            return ResolutionStatus.NOT_FOUND;
        }
        return isAccessible(link, clock) ? ResolutionStatus.OK : ResolutionStatus.GONE;
    }

    public static boolean isAccessible(Link link, Clock clock) {
        if (!link.isActive()) {
            return false;
        }
        Instant now = clock.instant();
        if (link.getExpiresAt() != null && !link.getExpiresAt().isAfter(now)) {
            return false;
        }
        if (link.getMaxClicks() != null && link.getClickCount() >= link.getMaxClicks()) {
            return false;
        }
        return true;
    }
}
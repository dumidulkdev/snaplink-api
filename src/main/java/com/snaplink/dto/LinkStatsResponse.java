package com.snaplink.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record LinkStatsResponse(
        long totalClicks,
        @Schema(description = "Click counts grouped by calendar day (UTC)")
        List<DailyClickCount> clicksPerDay,
        List<RankedCount> topReferrers,
        List<RankedCount> topUserAgents
) {
    public record DailyClickCount(String day, long count) {
    }

    public record RankedCount(String value, long count) {
    }
}
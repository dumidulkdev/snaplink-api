package com.snaplink.dto;

import java.util.List;

public record LinkStatsResponse(
        long totalClicks,
        List<DailyClickCount> clicksPerDay,
        List<RankedCount> topReferrers,
        List<RankedCount> topUserAgents
) {
    public record DailyClickCount(String day, long count) {
    }

    public record RankedCount(String value, long count) {
    }
}
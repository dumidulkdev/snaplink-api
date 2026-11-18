package com.snaplink.mapper;

import com.snaplink.config.SnapLinkProperties;
import com.snaplink.dto.LinkResponse;
import com.snaplink.entity.Link;
import org.springframework.stereotype.Component;

@Component
public class LinkMapper {

    private final SnapLinkProperties properties;

    public LinkMapper(SnapLinkProperties properties) {
        this.properties = properties;
    }

    public LinkResponse toResponse(Link link, Long totalClicks) {
        return new LinkResponse(
                link.getCode(),
                buildShortUrl(link.getCode()),
                link.getTargetUrl(),
                link.getCreatedAt(),
                link.getExpiresAt(),
                link.getMaxClicks(),
                link.isActive(),
                totalClicks
        );
    }

    public String buildShortUrl(String code) {
        String base = properties.getBaseUrl();
        if (base.endsWith("/")) {
            return base + code;
        }
        return base + "/" + code;
    }
}
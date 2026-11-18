package com.snaplink.service;

import com.snaplink.config.SnapLinkProperties;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

@Component
public class UrlValidator {

    private final SnapLinkProperties properties;

    public UrlValidator(SnapLinkProperties properties) {
        this.properties = properties;
    }

    public String validateAndNormalize(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            throw new IllegalArgumentException("URL is required");
        }
        String trimmed = rawUrl.trim();
        if (trimmed.length() > properties.getUrlMaxLength()) {
            throw new IllegalArgumentException("URL exceeds maximum length");
        }
        URI uri;
        try {
            uri = new URI(trimmed);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL");
        }
        String scheme = uri.getScheme();
        if (scheme == null) {
            throw new IllegalArgumentException("URL must include http or https scheme");
        }
        String normalizedScheme = scheme.toLowerCase(Locale.ROOT);
        if (!"http".equals(normalizedScheme) && !"https".equals(normalizedScheme)) {
            throw new IllegalArgumentException("Only http and https URLs are allowed");
        }
        if (uri.getHost() == null || uri.getHost().isBlank()) {
            throw new IllegalArgumentException("URL must have a host");
        }
        rejectSelfHost(uri);
        return trimmed;
    }

    public void validateCustomAlias(String alias) {
        if (alias == null || alias.length() < 3 || alias.length() > 30) {
            throw new IllegalArgumentException("Custom alias must be 3-30 characters");
        }
        if (!alias.matches("[A-Za-z0-9\\-_]+")) {
            throw new IllegalArgumentException("Custom alias contains invalid characters");
        }
        String lower = alias.toLowerCase(Locale.ROOT);
        if (properties.getReservedAliases().stream().anyMatch(r -> r.equalsIgnoreCase(lower))) {
            throw new IllegalArgumentException("Custom alias is reserved");
        }
    }

    private void rejectSelfHost(URI uri) {
        URI base;
        try {
            base = new URI(properties.getBaseUrl());
        } catch (URISyntaxException e) {
            return;
        }
        String baseHost = normalizeHost(base.getHost());
        String targetHost = normalizeHost(uri.getHost());
        if (baseHost != null && baseHost.equals(targetHost)) {
            throw new IllegalArgumentException("Links to this service are not allowed");
        }
    }

    private static String normalizeHost(String host) {
        if (host == null) {
            return null;
        }
        return host.toLowerCase(Locale.ROOT).replaceFirst("^www\\.", "");
    }
}
package com.snaplink.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.snaplink.dto.ErrorResponse;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.time.Duration;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RateLimitFilter extends OncePerRequestFilter {

    private final SnapLinkProperties properties;
    private final ClientIpResolver clientIpResolver;
    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final Map<String, Bucket> createBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> redirectBuckets = new ConcurrentHashMap<>();

    public RateLimitFilter(
            SnapLinkProperties properties,
            ClientIpResolver clientIpResolver,
            ObjectMapper objectMapper,
            Clock clock) {
        this.properties = properties;
        this.clientIpResolver = clientIpResolver;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        String method = request.getMethod();

        Bucket bucket = null;
        if ("POST".equalsIgnoreCase(method) && "/api/links".equals(path)) {
            bucket = createBuckets.computeIfAbsent(clientIpResolver.resolve(request), ip -> newBucket(properties.getRateLimit().getCreate()));
        } else if ("GET".equalsIgnoreCase(method) && isRedirectPath(path)) {
            bucket = redirectBuckets.computeIfAbsent(clientIpResolver.resolve(request), ip -> newBucket(properties.getRateLimit().getRedirect()));
        }

        if (bucket != null && !bucket.tryConsume(1)) {
            long waitSeconds = Math.max(1, Duration.ofNanos(
                    bucket.estimateAbilityToConsume(1).getNanosToWaitForRefill()).getSeconds());
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", String.valueOf(waitSeconds));
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            ErrorResponse body = new ErrorResponse(
                    "rate_limited",
                    "Too many requests",
                    Instant.now(clock),
                    path);
            objectMapper.writeValue(response.getOutputStream(), body);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isRedirectPath(String path) {
        if (path == null || path.equals("/") || path.startsWith("/api")
                || path.startsWith("/swagger") || path.startsWith("/v3")
                || path.startsWith("/actuator") || path.contains(".")) {
            return false;
        }
        return path.length() > 1;
    }

    private static Bucket newBucket(SnapLinkProperties.BucketProperties config) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(config.getCapacity())
                .refillGreedy(config.getRefillPerMinute(), Duration.ofMinutes(1))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }
}
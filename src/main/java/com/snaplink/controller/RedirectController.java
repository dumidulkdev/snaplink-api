package com.snaplink.controller;

import com.snaplink.config.ClientIpResolver;
import com.snaplink.service.RedirectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Set;

@RestController
@Tag(name = "Redirect", description = "Follow a short link")
public class RedirectController {

    private static final Set<String> RESERVED_ROOT_SEGMENTS = Set.of(
            "api", "swagger-ui", "swagger-ui.html", "v3", "actuator", "error", "favicon.ico");

    private final RedirectService redirectService;
    private final ClientIpResolver clientIpResolver;

    public RedirectController(RedirectService redirectService, ClientIpResolver clientIpResolver) {
        this.redirectService = redirectService;
        this.clientIpResolver = clientIpResolver;
    }

    @GetMapping("/{code}")
    @Operation(summary = "Redirect to target URL", description = "Records a click and responds with HTTP 302 when the link is active.")
    public ResponseEntity<Void> redirect(
            @PathVariable String code,
            HttpServletRequest request) {
        if (RESERVED_ROOT_SEGMENTS.contains(code.toLowerCase())) {
            return ResponseEntity.notFound().build();
        }
        String target = redirectService.resolveAndTrack(
                code,
                request.getHeader(HttpHeaders.REFERER),
                request.getHeader(HttpHeaders.USER_AGENT),
                clientIpResolver.resolve(request));
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(target)).build();
    }
}
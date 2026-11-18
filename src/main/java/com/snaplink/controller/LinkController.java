package com.snaplink.controller;

import com.snaplink.dto.CreateLinkRequest;
import com.snaplink.dto.LinkResponse;
import com.snaplink.dto.LinkStatsResponse;
import com.snaplink.dto.UpdateLinkRequest;
import com.snaplink.service.LinkService;
import com.snaplink.service.QrCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/links")
@Tag(name = "Links", description = "Create and manage short links")
public class LinkController {

    private final LinkService linkService;
    private final QrCodeService qrCodeService;

    public LinkController(LinkService linkService, QrCodeService qrCodeService) {
        this.linkService = linkService;
        this.qrCodeService = qrCodeService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a short link", description = "Shortens a long http(s) URL with optional alias, expiry and click cap.")
    public LinkResponse create(@Valid @RequestBody CreateLinkRequest request) {
        return linkService.createLink(request);
    }

    @GetMapping("/{code}")
    @Operation(summary = "Get link details", description = "Returns metadata and total click count for a short code.")
    public LinkResponse get(@PathVariable String code) {
        return linkService.getLink(code);
    }

    @GetMapping("/{code}/stats")
    @Operation(summary = "Link click analytics", description = "Total clicks, daily breakdown, top referrers and user agents.")
    public LinkStatsResponse stats(
            @PathVariable String code,
            @Parameter(description = "Number of days to include in the daily breakdown")
            @RequestParam(defaultValue = "30") int days) {
        return linkService.getStats(code, days);
    }

    @GetMapping(value = "/{code}/qr", produces = MediaType.IMAGE_PNG_VALUE)
    @Operation(summary = "QR code PNG", description = "Returns a PNG QR code encoding the short URL.")
    public ResponseEntity<byte[]> qr(
            @PathVariable String code,
            @RequestParam(required = false) Integer size) {
        byte[] png = qrCodeService.generatePng(code, size);
        linkService.getLink(code);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_PNG_VALUE)
                .body(png);
    }

    @PatchMapping("/{code}")
    @Operation(summary = "Update link", description = "Change expiry, max clicks or active flag. Evicts resolution cache.")
    public LinkResponse update(@PathVariable String code, @RequestBody UpdateLinkRequest request) {
        return linkService.updateLink(code, request);
    }

    @DeleteMapping("/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete link", description = "Removes the link and evicts cache.")
    public void delete(@PathVariable String code) {
        linkService.deleteLink(code);
    }
}
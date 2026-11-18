package com.snaplink.controller;

import com.snaplink.dto.CreateLinkRequest;
import com.snaplink.dto.LinkResponse;
import com.snaplink.dto.LinkStatsResponse;
import com.snaplink.service.LinkService;
import com.snaplink.service.QrCodeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/links")
public class LinkController {

    private final LinkService linkService;
    private final QrCodeService qrCodeService;

    public LinkController(LinkService linkService, QrCodeService qrCodeService) {
        this.linkService = linkService;
        this.qrCodeService = qrCodeService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LinkResponse create(@Valid @RequestBody CreateLinkRequest request) {
        return linkService.createLink(request);
    }

    @GetMapping("/{code}")
    public LinkResponse get(@PathVariable String code) {
        return linkService.getLink(code);
    }

    @GetMapping("/{code}/stats")
    public LinkStatsResponse stats(@PathVariable String code, @RequestParam(defaultValue = "30") int days) {
        return linkService.getStats(code, days);
    }

    @GetMapping(value = "/{code}/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> qr(@PathVariable String code, @RequestParam(required = false) Integer size) {
        byte[] png = qrCodeService.generatePng(code, size);
        linkService.getLink(code);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_PNG_VALUE)
                .body(png);
    }
}
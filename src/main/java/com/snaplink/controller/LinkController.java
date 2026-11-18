package com.snaplink.controller;

import com.snaplink.dto.CreateLinkRequest;
import com.snaplink.dto.LinkResponse;
import com.snaplink.service.LinkService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/links")
public class LinkController {

    private final LinkService linkService;

    public LinkController(LinkService linkService) {
        this.linkService = linkService;
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
}
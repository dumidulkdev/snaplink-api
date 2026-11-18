package com.snaplink.service;

import com.snaplink.config.SnapLinkProperties;
import com.snaplink.dto.CreateLinkRequest;
import com.snaplink.dto.LinkResponse;
import com.snaplink.entity.Link;
import com.snaplink.exception.ConflictException;
import com.snaplink.exception.ResourceNotFoundException;
import com.snaplink.mapper.LinkMapper;
import com.snaplink.repository.ClickEventRepository;
import com.snaplink.repository.LinkRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
public class LinkService {

    private final LinkRepository linkRepository;
    private final ClickEventRepository clickEventRepository;
    private final UrlValidator urlValidator;
    private final ShortCodeGenerator codeGenerator;
    private final SnapLinkProperties properties;
    private final LinkMapper linkMapper;
    private final Clock clock;

    public LinkService(
            LinkRepository linkRepository,
            ClickEventRepository clickEventRepository,
            UrlValidator urlValidator,
            ShortCodeGenerator codeGenerator,
            SnapLinkProperties properties,
            LinkMapper linkMapper,
            Clock clock) {
        this.linkRepository = linkRepository;
        this.clickEventRepository = clickEventRepository;
        this.urlValidator = urlValidator;
        this.codeGenerator = codeGenerator;
        this.properties = properties;
        this.linkMapper = linkMapper;
        this.clock = clock;
    }

    @Transactional
    public LinkResponse createLink(CreateLinkRequest request) {
        String targetUrl = urlValidator.validateAndNormalize(request.targetUrl());
        if (request.expiresAt() != null && !request.expiresAt().isAfter(clock.instant())) {
            throw new IllegalArgumentException("expiresAt must be in the future");
        }

        String code;
        if (request.customAlias() != null && !request.customAlias().isBlank()) {
            String alias = request.customAlias().trim();
            urlValidator.validateCustomAlias(alias);
            if (linkRepository.existsByCode(alias)) {
                throw new ConflictException("Custom alias is already taken");
            }
            code = alias;
        } else {
            code = allocateUniqueCode();
        }

        Link link = new Link(code, targetUrl, clock.instant(), request.expiresAt(), request.maxClicks());
        try {
            link = linkRepository.save(link);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Custom alias is already taken");
        }
        return linkMapper.toResponse(link, 0L);
    }

    private String allocateUniqueCode() {
        for (int attempt = 0; attempt < properties.getCodeCollisionRetries(); attempt++) {
            String candidate = codeGenerator.generate(properties.getCodeLength());
            if (!linkRepository.existsByCode(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Unable to allocate unique short code");
    }

    @Transactional(readOnly = true)
    public LinkResponse getLink(String code) {
        Link link = requireLink(code);
        long total = clickEventRepository.countByLinkId(link.getId());
        return linkMapper.toResponse(link, total);
    }

    private Link requireLink(String code) {
        return linkRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Link not found"));
    }
}
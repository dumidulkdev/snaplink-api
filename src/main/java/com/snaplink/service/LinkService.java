package com.snaplink.service;

import com.snaplink.config.SnapLinkProperties;
import com.snaplink.dto.CreateLinkRequest;
import com.snaplink.dto.LinkResponse;
import com.snaplink.dto.LinkStatsResponse;
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
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class LinkService {

    private static final int TOP_N = 10;

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

    @Transactional(readOnly = true)
    public LinkStatsResponse getStats(String code, int days) {
        if (days < 1 || days > 365) {
            throw new IllegalArgumentException("days must be between 1 and 365");
        }
        Link link = requireLink(code);
        long total = clickEventRepository.countByLinkId(link.getId());
        Instant since = clock.instant().minusSeconds(days * 86_400L);

        List<LinkStatsResponse.DailyClickCount> perDay = new ArrayList<>();
        for (Object[] row : clickEventRepository.countClicksPerDay(link.getId(), since)) {
            String day = formatDay(row[0]);
            long count = ((Number) row[1]).longValue();
            perDay.add(new LinkStatsResponse.DailyClickCount(day, count));
        }

        List<LinkStatsResponse.RankedCount> topReferrers = mapRanked(
                clickEventRepository.topReferrers(link.getId()), TOP_N);
        List<LinkStatsResponse.RankedCount> topUserAgents = mapRanked(
                clickEventRepository.topUserAgents(link.getId()), TOP_N);

        return new LinkStatsResponse(total, perDay, topReferrers, topUserAgents);
    }

    private Link requireLink(String code) {
        return linkRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Link not found"));
    }

    private static List<LinkStatsResponse.RankedCount> mapRanked(List<Object[]> rows, int limit) {
        return rows.stream()
                .limit(limit)
                .map(row -> new LinkStatsResponse.RankedCount(String.valueOf(row[0]), ((Number) row[1]).longValue()))
                .toList();
    }

    private static String formatDay(Object value) {
        if (value instanceof LocalDate localDate) {
            return localDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        if (value instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        if (value instanceof Instant instant) {
            return instant.atZone(ZoneOffset.UTC).toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        return String.valueOf(value);
    }
}
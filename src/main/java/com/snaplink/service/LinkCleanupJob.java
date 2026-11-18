package com.snaplink.service;

import com.snaplink.config.SnapLinkProperties;
import com.snaplink.entity.Link;
import com.snaplink.repository.LinkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;

@Component
public class LinkCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(LinkCleanupJob.class);

    private final LinkRepository linkRepository;
    private final LinkResolutionCache linkResolutionCache;
    private final SnapLinkProperties properties;
    private final Clock clock;

    public LinkCleanupJob(
            LinkRepository linkRepository,
            LinkResolutionCache linkResolutionCache,
            SnapLinkProperties properties,
            Clock clock) {
        this.linkRepository = linkRepository;
        this.linkResolutionCache = linkResolutionCache;
        this.properties = properties;
        this.clock = clock;
    }

    @Scheduled(cron = "${snaplink.cleanup.cron}")
    @Transactional
    public void cleanupExpiredLinks() {
        List<Link> expired = linkRepository.findExpiredActiveLinks(clock.instant());
        if (expired.isEmpty()) {
            return;
        }
        List<Long> ids = expired.stream().map(Link::getId).toList();
        if (properties.getCleanup().isPurgeInsteadOfDeactivate()) {
            linkRepository.deleteByIdIn(ids);
            log.info("Purged {} expired links", ids.size());
        } else {
            linkRepository.deactivateByIds(ids);
            log.info("Deactivated {} expired links", ids.size());
        }
        expired.forEach(link -> linkResolutionCache.evict(link.getCode()));
    }
}
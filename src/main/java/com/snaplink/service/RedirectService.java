package com.snaplink.service;

import com.snaplink.entity.ClickEvent;
import com.snaplink.entity.Link;
import com.snaplink.exception.LinkGoneException;
import com.snaplink.exception.ResourceNotFoundException;
import com.snaplink.repository.ClickEventRepository;
import com.snaplink.repository.LinkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
public class RedirectService {

    private final LinkRepository linkRepository;
    private final ClickEventRepository clickEventRepository;
    private final IpHashService ipHashService;
    private final Clock clock;

    public RedirectService(
            LinkRepository linkRepository,
            ClickEventRepository clickEventRepository,
            IpHashService ipHashService,
            Clock clock) {
        this.linkRepository = linkRepository;
        this.clickEventRepository = clickEventRepository;
        this.ipHashService = ipHashService;
        this.clock = clock;
    }

    @Transactional
    public String resolveAndTrack(String code, String referrer, String userAgent, String clientIp) {
        Link link = linkRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Link not found"));

        LinkRules.ResolutionStatus status = LinkRules.resolveStatus(link, clock);
        if (status == LinkRules.ResolutionStatus.GONE) {
            throw new LinkGoneException("Link is no longer available");
        }

        link.incrementClickCount();
        linkRepository.save(link);

        ClickEvent event = new ClickEvent(
                link,
                clock.instant(),
                truncate(referrer, 2048),
                truncate(userAgent, 1024),
                ipHashService.hash(clientIp));
        clickEventRepository.save(event);

        return link.getTargetUrl();
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}
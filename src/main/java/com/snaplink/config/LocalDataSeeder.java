package com.snaplink.config;

import com.snaplink.entity.Link;
import com.snaplink.repository.LinkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@Profile("local")
public class LocalDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(LocalDataSeeder.class);

    private final LinkRepository linkRepository;
    private final Clock clock;

    public LocalDataSeeder(LinkRepository linkRepository, Clock clock) {
        this.linkRepository = linkRepository;
        this.clock = clock;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (linkRepository.existsByCode("demo-docs")) {
            return;
        }
        Instant created = clock.instant();
        linkRepository.save(new Link(
                "demo-docs",
                "https://spring.io/projects/spring-boot",
                created,
                created.plus(365, ChronoUnit.DAYS),
                null));
        linkRepository.save(new Link(
                "demo-cv",
                "https://example.com/portfolio",
                created,
                null,
                50));
        log.info("Seeded demo short links: demo-docs, demo-cv");
    }
}
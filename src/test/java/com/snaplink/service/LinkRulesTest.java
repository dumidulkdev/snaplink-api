package com.snaplink.service;

import com.snaplink.entity.Link;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class LinkRulesTest {

  private final Clock clock = Clock.fixed(Instant.parse("2026-06-01T12:00:00Z"), ZoneOffset.UTC);

  @Test
  void inactiveLinkIsGone() {
      Link link = new Link("x", "https://example.com", clock.instant(), null, null);
      link.setActive(false);
      assertThat(LinkRules.isAccessible(link, clock)).isFalse();
  }

  @Test
  void expiredLinkIsGone() {
      Link link = new Link("x", "https://example.com", clock.instant(), clock.instant().minusSeconds(1), null);
      assertThat(LinkRules.isAccessible(link, clock)).isFalse();
  }

  @Test
  void maxClicksReachedIsGone() {
      Link link = new Link("x", "https://example.com", clock.instant(), null, 1);
      link.incrementClickCount();
      assertThat(LinkRules.isAccessible(link, clock)).isFalse();
  }
}
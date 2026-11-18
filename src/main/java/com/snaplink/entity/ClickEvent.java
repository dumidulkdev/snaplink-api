package com.snaplink.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "click_events")
public class ClickEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "link_id", nullable = false)
    private Link link;

    @Column(name = "clicked_at", nullable = false)
    private Instant clickedAt;

    @Column(length = 2048)
    private String referrer;

    @Column(name = "user_agent", length = 1024)
    private String userAgent;

    @Column(name = "ip_hash", nullable = false, length = 64)
    private String ipHash;

    protected ClickEvent() {
    }

    public ClickEvent(Link link, Instant clickedAt, String referrer, String userAgent, String ipHash) {
        this.link = link;
        this.clickedAt = clickedAt;
        this.referrer = referrer;
        this.userAgent = userAgent;
        this.ipHash = ipHash;
    }

    public Long getId() {
        return id;
    }

    public Link getLink() {
        return link;
    }

    public Instant getClickedAt() {
        return clickedAt;
    }

    public String getReferrer() {
        return referrer;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getIpHash() {
        return ipHash;
    }
}
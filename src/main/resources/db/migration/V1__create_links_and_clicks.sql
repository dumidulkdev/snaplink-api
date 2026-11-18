CREATE TABLE links (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(30) NOT NULL,
    target_url      VARCHAR(2048) NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at      TIMESTAMP WITH TIME ZONE,
    max_clicks      INTEGER,
    click_count     INTEGER NOT NULL DEFAULT 0,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_links_code UNIQUE (code)
);

CREATE INDEX idx_links_code ON links (code);
CREATE INDEX idx_links_expires_at ON links (expires_at);

CREATE TABLE click_events (
    id          BIGSERIAL PRIMARY KEY,
    link_id     BIGINT NOT NULL REFERENCES links (id) ON DELETE CASCADE,
    clicked_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    referrer    VARCHAR(2048),
    user_agent  VARCHAR(1024),
    ip_hash     VARCHAR(64) NOT NULL
);

CREATE INDEX idx_click_events_link_id ON click_events (link_id);
CREATE INDEX idx_click_events_clicked_at ON click_events (clicked_at);
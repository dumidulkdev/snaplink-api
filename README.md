# SnapLink API

SnapLink is a URL shortener REST API. Long links clutter social posts, résumés, and chat messages, and most sharing tools offer little insight into who clicks them. SnapLink turns a long `http` or `https` URL into a short, trackable link with optional expiry, click limits, analytics, QR codes, and per-IP rate limiting.

## Features

- Create short links with collision-safe Base62 codes or custom aliases
- Optional `expiresAt` and `maxClicks` with clear **410 Gone** behaviour when a link is unavailable
- **302** redirects with click tracking (referrer, user agent, hashed client IP—never raw IPs)
- Caffeine-backed resolution cache with explicit eviction on updates and redirects
- Per-link stats: totals, daily breakdown, top referrers and user agents
- PNG QR codes for short URLs
- Bucket4j rate limits on link creation and redirects (**429** with `Retry-After`)
- Scheduled cleanup for expired links
- OpenAPI/Swagger UI and Docker Compose stack

## Tech stack

| Layer | Technology |
|-------|------------|
| Runtime | Java 21, Spring Boot 3.4 |
| API | Spring Web, Bean Validation, springdoc-openapi |
| Data | Spring Data JPA, PostgreSQL, Flyway |
| Local dev | H2 in PostgreSQL compatibility mode (no Docker required) |
| Cache | Spring Cache + Caffeine |
| Rate limiting | Bucket4j servlet filter |
| QR codes | ZXing |
| Tests | JUnit 5, Mockito, MockMvc, Testcontainers (optional) |

## Quick start

### Maven (local profile, in-memory H2)

```bash
mvn spring-boot:run
```

The default `local` profile starts on port **8080**, runs Flyway migrations, and seeds demo links `demo-docs` and `demo-cv`.

### Docker Compose (app + PostgreSQL)

```bash
mvn -q -DskipTests package
docker compose up --build
```

### Swagger UI

[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## Environment variables (Docker profile)

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_HOST` | `postgres` | PostgreSQL host |
| `DB_PORT` | `5432` | PostgreSQL port |
| `DB_NAME` | `snaplink` | Database name |
| `DB_USER` | `snaplink` | Database user |
| `DB_PASSWORD` | `snaplink` | Database password |
| `SNAPLINK_BASE_URL` | `http://localhost:8080` | Public base URL used in short links and QR codes |
| `SNAPLINK_IP_HASH_SALT` | *(see compose file)* | Salt for SHA-256 client IP hashing |
| `SPRING_PROFILES_ACTIVE` | `docker` | Set automatically in the container image |

## API endpoints

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/links` | Create a short link |
| `GET` | `/api/links/{code}` | Link metadata and total clicks |
| `GET` | `/api/links/{code}/stats?days=30` | Analytics |
| `GET` | `/api/links/{code}/qr?size=300` | PNG QR code |
| `PATCH` | `/api/links/{code}` | Update expiry, max clicks, or active flag |
| `DELETE` | `/api/links/{code}` | Delete link |
| `GET` | `/{code}` | Redirect to target URL |

## Example requests

Create a link:

```bash
curl -s -X POST http://localhost:8080/api/links \
  -H 'Content-Type: application/json' \
  -d '{"targetUrl":"https://example.com/guides/spring-boot"}'
```

Follow the redirect (records a click):

```bash
curl -i http://localhost:8080/<code>
```

Custom alias:

```bash
curl -s -X POST http://localhost:8080/api/links \
  -H 'Content-Type: application/json' \
  -d '{"targetUrl":"https://example.com/cv","customAlias":"my-cv-2026"}'
```

Stats:

```bash
curl -s http://localhost:8080/api/links/<code>/stats?days=7
```

QR code:

```bash
curl -s -o qr.png "http://localhost:8080/api/links/<code>/qr?size=400"
```

Rate limit (after exceeding the create bucket for your IP):

```bash
curl -i -X POST http://localhost:8080/api/links \
  -H 'Content-Type: application/json' \
  -d '{"targetUrl":"https://example.com/another"}'
```

## Link rules

- **URL validation:** only `http`/`https`, max length 2048, cannot point at the service's own host
- **Custom alias:** 3–30 characters, `[A-Za-z0-9-_]`, reserved words (`api`, `swagger`, …) rejected; duplicate alias returns **409**
- **Expiry:** `expiresAt` must be in the future when created or updated; expired links return **410**
- **Max clicks:** when `click_count >= maxClicks`, redirects return **410**
- **Disabled:** `active=false` (via PATCH or scheduled cleanup) yields **410**
- **Unknown code:** **404**

## Configuration (`application.yml` / `snaplink.*`)

| Property | Default (local) | Purpose |
|----------|-----------------|---------|
| `snaplink.base-url` | `http://localhost:8080` | Short URL prefix |
| `snaplink.url-max-length` | `2048` | Target URL limit |
| `snaplink.code-length` | `8` | Random code length |
| `snaplink.rate-limit.create.*` | 20 capacity / 10 per min | Create endpoint bucket |
| `snaplink.rate-limit.redirect.*` | 100 capacity / 60 per min | Redirect bucket |
| `snaplink.cache.link-resolution-*` | 10000 entries / 30 min TTL | Caffeine settings |
| `snaplink.cleanup.cron` | hourly | Expired link job schedule |
| `snaplink.cleanup.purge-instead-of-deactivate` | `false` | Delete vs deactivate expired links |
| `snaplink.qr.*` | 100–1000 px, default 300 | QR size bounds |

## Project structure

```
src/main/java/com/snaplink/
  controller/   REST and redirect endpoints
  service/      Business logic, cache facade, QR, cleanup job
  repository/   JPA repositories and aggregate queries
  entity/       Link and ClickEvent
  dto/          Request/response records
  mapper/       Entity ↔ DTO mapping
  config/       Properties, cache, rate limit filter, OpenAPI, seeder
  exception/    Domain errors and global handler
src/main/resources/db/migration/   Flyway SQL
src/test/java/                     Unit and integration tests
```

## What I learned

- **Spring Cache + Caffeine:** caching link resolution speeds up redirects, but mutable fields (click counts, `active`) require deliberate eviction after PATCH, DELETE, redirects, and cleanup.
- **Bucket4j servlet filter:** separate token buckets per client IP and route class (`POST /api/links` vs `GET /{code}`) give simple abuse protection with standard **429** responses.
- **JPA aggregate queries:** native SQL `GROUP BY` powers daily click charts and ranked referrers/user agents without pulling every event into memory.
- **Binary responses:** returning `image/png` QR bodies needs explicit `produces` and `Content-Type` headers alongside JSON endpoints in the same app.
- **`@Scheduled` maintenance:** a cron-driven job deactivates or purges expired rows and keeps the cache consistent.
- **Collision-safe codes:** a unique DB constraint plus bounded random retries makes Base62 generation straightforward in practice.

## License

[MIT License](LICENSE). Developed by Dumidu Lakshan.
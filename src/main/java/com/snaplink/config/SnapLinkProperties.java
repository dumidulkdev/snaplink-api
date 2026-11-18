package com.snaplink.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "snaplink")
public class SnapLinkProperties {

    private String baseUrl = "http://localhost:8080";
    private int urlMaxLength = 2048;
    private int codeLength = 8;
    private int codeCollisionRetries = 5;
    private List<String> reservedAliases = List.of();
    private RateLimitProperties rateLimit = new RateLimitProperties();
    private CacheProperties cache = new CacheProperties();
    private CleanupProperties cleanup = new CleanupProperties();
    private QrProperties qr = new QrProperties();
    private String ipHashSalt = "local-salt";

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public int getUrlMaxLength() {
        return urlMaxLength;
    }

    public void setUrlMaxLength(int urlMaxLength) {
        this.urlMaxLength = urlMaxLength;
    }

    public int getCodeLength() {
        return codeLength;
    }

    public void setCodeLength(int codeLength) {
        this.codeLength = codeLength;
    }

    public int getCodeCollisionRetries() {
        return codeCollisionRetries;
    }

    public void setCodeCollisionRetries(int codeCollisionRetries) {
        this.codeCollisionRetries = codeCollisionRetries;
    }

    public List<String> getReservedAliases() {
        return reservedAliases;
    }

    public void setReservedAliases(List<String> reservedAliases) {
        this.reservedAliases = reservedAliases;
    }

    public RateLimitProperties getRateLimit() {
        return rateLimit;
    }

    public void setRateLimit(RateLimitProperties rateLimit) {
        this.rateLimit = rateLimit;
    }

    public CacheProperties getCache() {
        return cache;
    }

    public void setCache(CacheProperties cache) {
        this.cache = cache;
    }

    public CleanupProperties getCleanup() {
        return cleanup;
    }

    public void setCleanup(CleanupProperties cleanup) {
        this.cleanup = cleanup;
    }

    public QrProperties getQr() {
        return qr;
    }

    public void setQr(QrProperties qr) {
        this.qr = qr;
    }

    public String getIpHashSalt() {
        return ipHashSalt;
    }

    public void setIpHashSalt(String ipHashSalt) {
        this.ipHashSalt = ipHashSalt;
    }

    public static class RateLimitProperties {
        private BucketProperties create = new BucketProperties(20, 10);
        private BucketProperties redirect = new BucketProperties(100, 60);

        public BucketProperties getCreate() {
            return create;
        }

        public void setCreate(BucketProperties create) {
            this.create = create;
        }

        public BucketProperties getRedirect() {
            return redirect;
        }

        public void setRedirect(BucketProperties redirect) {
            this.redirect = redirect;
        }
    }

    public static class BucketProperties {
        private long capacity;
        private long refillPerMinute;

        public BucketProperties() {
        }

        public BucketProperties(long capacity, long refillPerMinute) {
            this.capacity = capacity;
            this.refillPerMinute = refillPerMinute;
        }

        public long getCapacity() {
            return capacity;
        }

        public void setCapacity(long capacity) {
            this.capacity = capacity;
        }

        public long getRefillPerMinute() {
            return refillPerMinute;
        }

        public void setRefillPerMinute(long refillPerMinute) {
            this.refillPerMinute = refillPerMinute;
        }
    }

    public static class CacheProperties {
        private long linkResolutionMaxSize = 10_000;
        private long linkResolutionTtlMinutes = 30;

        public long getLinkResolutionMaxSize() {
            return linkResolutionMaxSize;
        }

        public void setLinkResolutionMaxSize(long linkResolutionMaxSize) {
            this.linkResolutionMaxSize = linkResolutionMaxSize;
        }

        public long getLinkResolutionTtlMinutes() {
            return linkResolutionTtlMinutes;
        }

        public void setLinkResolutionTtlMinutes(long linkResolutionTtlMinutes) {
            this.linkResolutionTtlMinutes = linkResolutionTtlMinutes;
        }
    }

    public static class CleanupProperties {
        private String cron = "0 0 * * * *";
        private boolean purgeInsteadOfDeactivate = false;

        public String getCron() {
            return cron;
        }

        public void setCron(String cron) {
            this.cron = cron;
        }

        public boolean isPurgeInsteadOfDeactivate() {
            return purgeInsteadOfDeactivate;
        }

        public void setPurgeInsteadOfDeactivate(boolean purgeInsteadOfDeactivate) {
            this.purgeInsteadOfDeactivate = purgeInsteadOfDeactivate;
        }
    }

    public static class QrProperties {
        private int defaultSize = 300;
        private int minSize = 100;
        private int maxSize = 1000;

        public int getDefaultSize() {
            return defaultSize;
        }

        public void setDefaultSize(int defaultSize) {
            this.defaultSize = defaultSize;
        }

        public int getMinSize() {
            return minSize;
        }

        public void setMinSize(int minSize) {
            this.minSize = minSize;
        }

        public int getMaxSize() {
            return maxSize;
        }

        public void setMaxSize(int maxSize) {
            this.maxSize = maxSize;
        }
    }
}
package com.snaplink.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.snaplink.service.LinkResolutionCache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(SnapLinkProperties properties) {
        CaffeineCacheManager manager = new CaffeineCacheManager(LinkResolutionCache.CACHE_NAME);
        manager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(properties.getCache().getLinkResolutionMaxSize())
                .expireAfterWrite(properties.getCache().getLinkResolutionTtlMinutes(), TimeUnit.MINUTES)
                .recordStats());
        return manager;
    }
}
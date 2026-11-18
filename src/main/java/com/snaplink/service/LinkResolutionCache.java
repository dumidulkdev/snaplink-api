package com.snaplink.service;

import com.snaplink.entity.Link;
import com.snaplink.repository.LinkRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LinkResolutionCache {

    public static final String CACHE_NAME = "linkResolution";

    private final LinkRepository linkRepository;

    public LinkResolutionCache(LinkRepository linkRepository) {
        this.linkRepository = linkRepository;
    }

    @Cacheable(cacheNames = CACHE_NAME, key = "#code")
    public Optional<Link> findByCode(String code) {
        return linkRepository.findByCode(code);
    }

    @CacheEvict(cacheNames = CACHE_NAME, key = "#code")
    public void evict(String code) {
        // eviction handled by annotation
    }

    @CacheEvict(cacheNames = CACHE_NAME, allEntries = true)
    public void evictAll() {
        // eviction handled by annotation
    }
}
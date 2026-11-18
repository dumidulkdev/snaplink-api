package com.snaplink.service;

import com.snaplink.config.SnapLinkProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UrlValidatorTest {

    private UrlValidator validator;

    @BeforeEach
    void setUp() {
        SnapLinkProperties properties = new SnapLinkProperties();
        properties.setBaseUrl("http://localhost:8080");
        properties.setUrlMaxLength(2048);
        properties.setReservedAliases(List.of("api", "swagger"));
        validator = new UrlValidator(properties);
    }

    @Test
    void rejectsNonHttpSchemes() {
        assertThatThrownBy(() -> validator.validateAndNormalize("ftp://example.com"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsLinksToOwnHost() {
        assertThatThrownBy(() -> validator.validateAndNormalize("http://localhost:8080/secret"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsReservedCustomAlias() {
        assertThatThrownBy(() -> validator.validateCustomAlias("api"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsInvalidCustomAliasCharacters() {
        assertThatThrownBy(() -> validator.validateCustomAlias("bad alias"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
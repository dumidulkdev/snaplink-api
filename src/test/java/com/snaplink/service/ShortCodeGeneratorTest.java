package com.snaplink.service;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ShortCodeGeneratorTest {

    private final ShortCodeGenerator generator = new ShortCodeGenerator();

    @Test
    void generatesBase62CodesOfRequestedLength() {
        String code = generator.generate(8);
        assertThat(code).hasSize(8).matches("[0-9A-Za-z]+");
    }

    @Test
    void generatesMostlyUniqueCodes() {
        Set<String> codes = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            codes.add(generator.generate(10));
        }
        assertThat(codes).hasSizeGreaterThan(95);
    }
}
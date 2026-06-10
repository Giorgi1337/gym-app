package com.gym.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PasswordGeneratorTest {
    private final PasswordGenerator generator = new PasswordGenerator();

    @Test
    void generatePassword() {
        assertThat(generator.generate())
                .isNotNull()
                .hasSize(10)
                .matches("[A-Za-z0-9]{10}");
    }

}

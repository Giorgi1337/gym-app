package com.gym.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PasswordGeneratorTest {

    @Test
    void generatePassword() {
        assertThat(PasswordGenerator.generate())
                .isNotNull()
                .hasSize(10)
                .matches("[A-Za-z0-9]{10}");
    }

}

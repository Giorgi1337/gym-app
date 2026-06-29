package com.gym.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class NameUtilsTest {

    @Test
    void nameUtils() {
        assertThat(
                NameUtils.normalize("  joHn   doe  "))
                .isEqualTo("John Doe");
    }

    @Test
    void fullName() {
        assertThat(NameUtils.fullName("John", "Doe"))
                .isEqualTo("John Doe");
    }

}

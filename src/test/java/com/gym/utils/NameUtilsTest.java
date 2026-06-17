package com.gym.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class NameUtilsTest {

    @Test
    void nameUtils() {
        assertThat(
                NameUtils.normalize("  joHn   doe  "))
                .isEqualTo("John Doe");
    }

}

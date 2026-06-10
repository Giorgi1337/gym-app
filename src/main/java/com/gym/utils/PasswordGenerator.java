package com.gym.utils;

import org.apache.commons.text.RandomStringGenerator;
import org.springframework.stereotype.Component;

@Component
public class PasswordGenerator {

    private static final int PASSWORD_LENGTH = 10;

    private final RandomStringGenerator generator = new RandomStringGenerator.Builder()
            .withinRange(
                    new char[]{'0', '9'},
                    new char[]{'A', 'Z'},
                    new char[]{'a', 'z'}
            )
            .get();

    public String generate() {
        return generator.generate(PASSWORD_LENGTH);
    }
}

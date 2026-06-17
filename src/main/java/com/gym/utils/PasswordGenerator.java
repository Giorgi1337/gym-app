package com.gym.utils;

import org.apache.commons.text.RandomStringGenerator;

public final class PasswordGenerator {

    private PasswordGenerator() {}

    private static final int PASSWORD_LENGTH = 10;

    private static final RandomStringGenerator generator = new RandomStringGenerator.Builder()
            .withinRange(
                    new char[]{'0', '9'},
                    new char[]{'A', 'Z'},
                    new char[]{'a', 'z'}
            )
            .get();

    public static String generate() {
        return generator.generate(PASSWORD_LENGTH);
    }
}

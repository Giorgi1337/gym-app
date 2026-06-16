package com.gym.utils;

import org.apache.commons.lang3.StringUtils;

public final class NameUtils {

    public static final String NAME_REGEX = "^[A-Za-zÀ-ÖØ-öø-ÿ]{2,}(?:[ '-][A-Za-zÀ-ÖØ-öø-ÿ]{2,})*$";

    private NameUtils() {}

    public static String normalizeAndCapitalize(String name) {
        String normalized = StringUtils.normalizeSpace(name).toLowerCase();
        String[] words = normalized.split(" ");
        for (int i = 0; i < words.length; i++) {
            words[i] = StringUtils.capitalize(words[i]);
        }
        return String.join(" ", words);
    }
}

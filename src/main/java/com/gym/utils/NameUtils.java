package com.gym.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class NameUtils {

    public static final String NAME_REGEX = "^[A-Za-zÀ-ÖØ-öø-ÿ]{2,}(?:[ '-][A-Za-zÀ-ÖØ-öø-ÿ]{2,})*$";

    private NameUtils() {}

    public static String normalize(String name) {
        if (name == null) return null;
        String trimmed = StringUtils.normalizeSpace(name);
        if (trimmed.isEmpty()) return trimmed;
        return Arrays.stream(trimmed.split(" "))
                .map(word -> StringUtils.capitalize(word.toLowerCase()))
                .collect(Collectors.joining(" "));
    }
}
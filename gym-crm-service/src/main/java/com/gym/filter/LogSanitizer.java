package com.gym.filter;

import java.util.regex.Pattern;

/**
 * Masks sensitive field values in JSON request/response bodies before they're
 * written to logs. Regex-based scrubbing — intentionally simple, not a JSON
 * parser, since this only needs to catch known field names.
 */
public final class LogSanitizer {

    private static final String MASK = "***MASKED***";

    private static final Pattern[] SENSITIVE_FIELD_PATTERNS = {
            field("password"),
            field("newPassword"),
            field("oldPassword"),
            field("token"),
    };

    private LogSanitizer() {}

    static String sanitize(String body) {
        if (body == null || body.isBlank()) {
            return body;
        }
        String result = body;
        for (Pattern pattern : SENSITIVE_FIELD_PATTERNS) {
            result = pattern.matcher(result).replaceAll("$1" + MASK + "$2");
        }
        return result;
    }

    private static Pattern field(String name) {
        return Pattern.compile("(\"" + name + "\"\\s*:\\s*\")[^\"]*(\")", Pattern.CASE_INSENSITIVE);
    }
}
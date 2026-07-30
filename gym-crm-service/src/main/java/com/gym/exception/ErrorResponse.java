package com.gym.exception;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
        String traceId,
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldError> details
) {
    public record FieldError(
            String field,
            String message
    ) {}
}
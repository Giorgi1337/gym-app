package com.gym.workload.exception;

import java.time.Instant;

public record ErrorResponse(
        String transactionId,
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {}
package com.gym.filter;

import org.slf4j.MDC;

import java.util.UUID;

/**
 * Central definition of the transactionId concept: the MDC key it lives under,
 * the header used to propagate it to/from clients and downstream services, and
 * helpers to read/generate it. Keeping this in one place avoids magic-string
 * drift between the filter, the exception handler, and any future consumer.
 */
public final class TransactionContext {

    public static final String MDC_KEY = "transactionId";
    public static final String HEADER_NAME = "X-Transaction-Id";

    private TransactionContext() {}

    public static String currentOrNew(String incomingHeaderValue) {
        if (incomingHeaderValue != null && !incomingHeaderValue.isBlank()) {
            return incomingHeaderValue;
        }
        return UUID.randomUUID().toString();
    }

    public static String current() {
        return MDC.get(MDC_KEY);
    }
}
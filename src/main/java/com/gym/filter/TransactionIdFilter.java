package com.gym.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;

import java.io.IOException;

/**
 * Establishes the transaction-level logging context for every incoming request.
 *
 * <p>Generates (or honours an inbound) transactionId and stores it in SLF4J's MDC,
 * so every log statement emitted while handling this request — across filters,
 * controllers, services, and repositories — is automatically tagged with it via
 * the logback pattern's {@code %X{transactionId}}. The same id is echoed back as
 * a response header so callers can correlate client-side logs, and so it can be
 * forwarded as a request header to downstream services for distributed tracing.
 *
 * <p>Must run before {@link RequestResponseLoggingFilter} in the filter chain.
 */
public class TransactionIdFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String transactionId = TransactionContext.currentOrNew(
                httpRequest.getHeader(TransactionContext.HEADER_NAME));

        try {
            MDC.put(TransactionContext.MDC_KEY, transactionId);
            httpResponse.setHeader(TransactionContext.HEADER_NAME, transactionId);
            chain.doFilter(request, response);
        } finally {
            // Thread pools reuse threads across unrelated requests — without this,
            // a stale transactionId could leak into the next request handled by this thread.
            MDC.remove(TransactionContext.MDC_KEY);
        }
    }
}
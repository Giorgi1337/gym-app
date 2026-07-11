package com.gym.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class RequestResponseLoggingFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger("com.gym.filter.RequestResponseLog");

    /** Paths excluded from body logging — static/doc assets, not business traffic. */
    private static final Set<String> EXCLUDED_PREFIXES = Set.of("/swagger-ui", "/v3/api-docs", "/scalar");

    /** Caps how many request-body bytes are buffered in memory purely for logging. */
    private static final int MAX_LOGGED_BODY_BYTES = 10_000;

    private static final int HTTP_STATUS_ERROR_THRESHOLD = 400;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (isExcluded(httpRequest.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(httpRequest, MAX_LOGGED_BODY_BYTES);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(httpResponse);

        long startNanos = System.nanoTime();
        try {
            chain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            long durationMs = (System.nanoTime() - startNanos) / 1_000_000;
            logRequest(wrappedRequest);
            logResponse(wrappedResponse, durationMs);
            // Mandatory: wrapping the response intercepts its output stream, so the
            // cached bytes must be explicitly copied back or the client gets nothing.
            wrappedResponse.copyBodyToResponse();
        }
    }

    private boolean isExcluded(String path) {
        return EXCLUDED_PREFIXES.stream().anyMatch(path::startsWith);
    }

    private void logRequest(ContentCachingRequestWrapper request) {
        String body = readBody(request.getContentAsByteArray());
        log.info("REQUEST method = {} uri = {} body = {}",
                request.getMethod(),
                request.getRequestURI() + queryString(request),
                LogSanitizer.sanitize(body));
    }

    private void logResponse(ContentCachingResponseWrapper response, long durationMs) {
        int status = response.getStatus();
        String body = readBody(response.getContentAsByteArray());
        String sanitizedBody = LogSanitizer.sanitize(body);

        if (status >= HTTP_STATUS_ERROR_THRESHOLD) {
            log.warn("RESPONSE status = {} durationMs = {} body = {}", status, durationMs, sanitizedBody);
        } else {
            log.info("RESPONSE status = {} durationMs = {} body = {}", status, durationMs, sanitizedBody);
        }
    }

    private String readBody(byte[] content) {
        if (content == null || content.length == 0) {
            return "";
        }
        return new String(content, StandardCharsets.UTF_8);
    }

    private String queryString(HttpServletRequest request) {
        String qs = request.getQueryString();
        return qs == null ? "" : "?" + qs;
    }
}
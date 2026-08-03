package com.gym.workload.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class RequestResponseLoggingFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger("com.gym.workload.filter.RequestResponseLog");
    private static final int MAX_LOGGED_BODY_BYTES = 10_000;
    private static final int HTTP_STATUS_ERROR_THRESHOLD = 400;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(httpRequest, MAX_LOGGED_BODY_BYTES);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper((HttpServletResponse) response);

        long startNanos = System.nanoTime();
        try {
            chain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            long durationMs = (System.nanoTime() - startNanos) / 1_000_000;
            log.info("REQUEST method = {} uri = {} body = {}",
                    wrappedRequest.getMethod(), wrappedRequest.getRequestURI(),
                    new String(wrappedRequest.getContentAsByteArray(), StandardCharsets.UTF_8));

            int status = wrappedResponse.getStatus();
            String body = new String(wrappedResponse.getContentAsByteArray(), StandardCharsets.UTF_8);
            if (status >= HTTP_STATUS_ERROR_THRESHOLD) {
                log.warn("RESPONSE status = {} durationMs = {} body = {}", status, durationMs, body);
            } else {
                log.info("RESPONSE status = {} durationMs = {} body = {}", status, durationMs, body);
            }
            wrappedResponse.copyBodyToResponse();
        }
    }
}
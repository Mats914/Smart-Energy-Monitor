package com.energy.config;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple in-process rate limiter: 60 requests / minute per IP.
 * In production, replace with Redis-based solution (e.g. Bucket4j + Redis).
 */
@Component
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final int    MAX_REQUESTS = 60;
    private static final long   WINDOW_MS    = 60_000L;

    private final Map<String, RequestCounter> counters = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String ip = resolveIp(request);
        RequestCounter counter = counters.computeIfAbsent(ip, k -> new RequestCounter());

        if (!counter.allowRequest()) {
            log.warn("Rate limit exceeded for IP: {}", ip);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write(
                "{\"status\":429,\"error\":\"Too Many Requests\",\"message\":\"Rate limit: 60 req/min\"}"
            );
            return;
        }

        chain.doFilter(request, response);
    }

    private String resolveIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return (forwarded != null && !forwarded.isBlank())
            ? forwarded.split(",")[0].trim()
            : request.getRemoteAddr();
    }

    private static class RequestCounter {
        private final AtomicInteger count = new AtomicInteger(0);
        private volatile long windowStart  = Instant.now().toEpochMilli();

        boolean allowRequest() {
            long now = Instant.now().toEpochMilli();
            if (now - windowStart > WINDOW_MS) {
                count.set(0);
                windowStart = now;
            }
            return count.incrementAndGet() <= MAX_REQUESTS;
        }
    }
}

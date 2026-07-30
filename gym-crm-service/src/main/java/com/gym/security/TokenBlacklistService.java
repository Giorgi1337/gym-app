package com.gym.security;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenBlacklistService {

    private final Map<String, Instant> blacklisted = new ConcurrentHashMap<>();

    public void blacklist(String token, Date expiresAt) {
        blacklisted.put(token, expiresAt.toInstant());
    }

    public boolean isBlacklisted(String token) {
        cleanupExpired();
        return blacklisted.containsKey(token);
    }

    private void cleanupExpired() {
        blacklisted.entrySet().removeIf(e -> e.getValue().isBefore(Instant.now()));
    }
}
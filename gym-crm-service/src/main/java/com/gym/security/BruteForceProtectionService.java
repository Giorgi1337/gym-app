package com.gym.security;

import com.gym.exception.AccountLockedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class BruteForceProtectionService {

    private final int maxAttempts;
    private final Duration lockDuration;

    private final Map<String, AttemptInfo> attemptsByUsername = new ConcurrentHashMap<>();

    public BruteForceProtectionService(
            @Value("${security.brute-force.max-attempts}") int maxAttempts,
            @Value("${security.brute-force.lock-duration-minutes}") long lockDurationMinutes) {
        this.maxAttempts = maxAttempts;
        this.lockDuration = Duration.ofMinutes(lockDurationMinutes);
    }

    public void checkNotBlocked(String username) {
        AttemptInfo info = attemptsByUsername.get(username);
        if (info == null || info.lockedUntil() == null) {
            return;
        }

        if (Instant.now().isBefore(info.lockedUntil())) {
            Duration remaining = Duration.between(Instant.now(), info.lockedUntil());

            long minutesLeft = (remaining.getSeconds() + 59) / 60;

            throw new AccountLockedException(
                    "Account temporarily locked due to failed login attempts. Try again in "
                            + minutesLeft + " minute" + (minutesLeft == 1 ? "" : "s") + ".");
        }

        attemptsByUsername.remove(username);
    }

    public void registerFailure(String username) {
        attemptsByUsername.compute(username, (u, info) -> {
            int attempts = (info == null ? 0 : info.attempts()) + 1;
            Instant lockedUntil = attempts >= maxAttempts ? Instant.now().plus(lockDuration) : null;
            return new AttemptInfo(attempts, lockedUntil);
        });
    }

    public void registerSuccess(String username) {
        attemptsByUsername.remove(username);
    }

    private record AttemptInfo(int attempts, Instant lockedUntil) {
    }
}
package com.studymatch.security;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes do limite de tentativas de login (#82).
 */
class LoginThrottleTest {

    private Instant now = Instant.parse("2026-11-09T10:00:00Z");
    private final Clock clock = new Clock() {
        @Override public ZoneOffset getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return now; }
    };
    private final LoginThrottle throttle = new LoginThrottle(3, Duration.ofMinutes(15), clock);

    @Test
    void blocksAfterTheMaximumFailuresIgnoringCase() {
        throttle.recordFailure("Admin");
        throttle.recordFailure("admin");
        assertFalse(throttle.isBlocked("admin"));

        throttle.recordFailure(" ADMIN ");
        assertTrue(throttle.isBlocked("admin"));
        assertFalse(throttle.isBlocked("outro"));
    }

    @Test
    void unblocksWhenTheFailuresLeaveTheWindow() {
        for (int i = 0; i < 3; i++) {
            throttle.recordFailure("admin");
        }
        now = now.plus(Duration.ofMinutes(15)).plusSeconds(1);

        assertFalse(throttle.isBlocked("admin"));
    }

    @Test
    void successClearsTheFailures() {
        throttle.recordFailure("admin");
        throttle.recordFailure("admin");
        throttle.recordSuccess("admin");
        throttle.recordFailure("admin");

        assertFalse(throttle.isBlocked("admin"));
    }
}

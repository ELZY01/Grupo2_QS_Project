package com.studymatch.security;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes dos tokens de sessão.
 */
class TokenServiceTest {

    private final MutableClock clock = new MutableClock(Instant.parse("2026-11-09T10:00:00Z"));
    private final TokenService service = new TokenService(Duration.ofHours(8), clock);

    @Test
    void issuesARandomTokenThatResolvesToTheUser() {
        String a = service.issue(1L);
        String b = service.issue(1L);

        assertNotEquals(a, b);
        assertTrue(a.length() >= 40);
        assertEquals(Optional.of(1L), service.resolve(a));
    }

    @Test
    void rejectsUnknownBlankAndRevokedTokens() {
        String token = service.issue(2L);
        service.revoke(token);

        assertTrue(service.resolve(token).isEmpty());
        assertTrue(service.resolve("inventado").isEmpty());
        assertTrue(service.resolve("").isEmpty());
        assertTrue(service.resolve(null).isEmpty());
    }

    @Test
    void expiresAfterTheTtl() {
        String token = service.issue(3L);

        clock.advance(Duration.ofHours(8));
        assertEquals(Optional.of(3L), service.resolve(token));

        clock.advance(Duration.ofSeconds(1));
        assertTrue(service.resolve(token).isEmpty());
    }

    private static final class MutableClock extends Clock {
        private Instant now;

        MutableClock(Instant now) {
            this.now = now;
        }

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}

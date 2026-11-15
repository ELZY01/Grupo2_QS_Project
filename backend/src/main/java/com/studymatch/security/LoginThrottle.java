package com.studymatch.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limita as tentativas de login falhadas por utilizador (#82: ataque de força bruta).
 *
 * Depois de N falhas dentro da janela, o login desse utilizador fica bloqueado até a
 * falha mais antiga sair da janela. Um login certo limpa o contador.
 */
@Component
public class LoginThrottle {

    private final int maxFailures;
    private final Duration window;
    private final Clock clock;
    private final Map<String, Deque<Instant>> failures = new ConcurrentHashMap<>();

    @Autowired
    public LoginThrottle(@Value("${studymatch.auth.max-failures:5}") int maxFailures,
                         @Value("${studymatch.auth.lock-window:PT15M}") Duration window) {
        this(maxFailures, window, Clock.systemUTC());
    }

    LoginThrottle(int maxFailures, Duration window, Clock clock) {
        this.maxFailures = maxFailures;
        this.window = window;
        this.clock = clock;
    }

    /**
     * O utilizador está bloqueado por demasiadas falhas recentes?
     */
    public boolean isBlocked(String username) {
        Deque<Instant> recent = failures.get(key(username));
        if (recent == null) {
            return false;
        }
        synchronized (recent) {
            prune(recent);
            return recent.size() >= maxFailures;
        }
    }

    public void recordFailure(String username) {
        Deque<Instant> recent = failures.computeIfAbsent(key(username), k -> new ArrayDeque<>());
        synchronized (recent) {
            prune(recent);
            recent.addLast(clock.instant());
        }
    }

    public void recordSuccess(String username) {
        failures.remove(key(username));
    }

    private void prune(Deque<Instant> recent) {
        Instant limit = clock.instant().minus(window);
        while (!recent.isEmpty() && recent.peekFirst().isBefore(limit)) {
            recent.removeFirst();
        }
    }

    private static String key(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }
}

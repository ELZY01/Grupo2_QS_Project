package com.studymatch.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tokens de sessão opacos (aleatórios, sem dados lá dentro).
 *
 * Ficam em memória: ao reiniciar o backend, toda a gente tem de voltar a entrar.
 * Para várias instâncias do backend seria preciso guardá-los na base de dados.
 */
@Service
public class TokenService {

    private static final int TOKEN_BYTES = 32;

    private final SecureRandom random = new SecureRandom();
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private final Duration ttl;
    private final Clock clock;

    @Autowired
    public TokenService(@Value("${studymatch.auth.token-ttl:PT8H}") Duration ttl) {
        this(ttl, Clock.systemUTC());
    }

    TokenService(Duration ttl, Clock clock) {
        this.ttl = ttl;
        this.clock = clock;
    }

    /**
     * Cria um token novo para a conta indicada.
     */
    public String issue(Long userId) {
        byte[] bytes = new byte[TOKEN_BYTES];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        sessions.put(token, new Session(userId, clock.instant().plus(ttl)));
        return token;
    }

    /**
     * Devolve a conta associada ao token, se o token existir e não tiver expirado.
     */
    public Optional<Long> resolve(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        Session session = sessions.get(token);
        if (session == null) {
            return Optional.empty();
        }
        if (clock.instant().isAfter(session.expiresAt())) {
            sessions.remove(token);
            return Optional.empty();
        }
        return Optional.of(session.userId());
    }

    /**
     * Termina a sessão (logout).
     */
    public void revoke(String token) {
        if (token != null) {
            sessions.remove(token);
        }
    }

    private record Session(Long userId, Instant expiresAt) {
    }
}

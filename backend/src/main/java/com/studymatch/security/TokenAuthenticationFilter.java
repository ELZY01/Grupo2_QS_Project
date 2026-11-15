package com.studymatch.security;

import com.studymatch.repository.AppUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Lê o cabeçalho "Authorization: Bearer <token>" e, se o token for válido,
 * coloca o utilizador no contexto de segurança.
 * Sem token (ou com token inválido) o pedido segue como anónimo.
 */
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIX = "Bearer ";

    private final TokenService tokenService;
    private final AppUserRepository userRepository;

    public TokenAuthenticationFilter(TokenService tokenService, AppUserRepository userRepository) {
        this.tokenService = tokenService;
        this.userRepository = userRepository;
    }

    /**
     * Extrai o token do cabeçalho Authorization, ou null.
     */
    public static String extractToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(PREFIX)) {
            return null;
        }
        return header.substring(PREFIX.length()).trim();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String token = extractToken(request);
        tokenService.resolve(token)
                .flatMap(userRepository::findById)
                .map(AuthenticatedUser::from)
                .ifPresent(user -> {
                    var authentication = new UsernamePasswordAuthenticationToken(
                            user, null, List.of(new SimpleGrantedAuthority(user.role().authority())));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                });

        chain.doFilter(request, response);
    }
}

package com.studymatch.controller;

import com.studymatch.dto.LoginRequest;
import com.studymatch.dto.LoginResponse;
import com.studymatch.dto.RegisterRequest;
import com.studymatch.security.AuthenticatedUser;
import com.studymatch.security.TokenAuthenticationFilter;
import com.studymatch.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Login, registo do aluno e sessão atual.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * POST /api/auth/login
     * Devolve o token da sessão. Credenciais erradas → 401.
     */
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /**
     * POST /api/auth/register
     * Regista um aluno com palavra-passe e inicia a sessão (201).
     */
    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    /**
     * GET /api/auth/me
     * Quem tem a sessão iniciada.
     */
    @GetMapping("/me")
    public AuthenticatedUser me(@AuthenticationPrincipal AuthenticatedUser user) {
        return user;
    }

    /**
     * POST /api/auth/logout
     * Invalida o token atual (204).
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        authService.logout(TokenAuthenticationFilter.extractToken(request));
        return ResponseEntity.noContent().build();
    }
}

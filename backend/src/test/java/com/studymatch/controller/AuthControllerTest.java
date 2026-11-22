
package com.studymatch.controller;

import com.studymatch.config.SecurityConfig;
import com.studymatch.dto.LoginRequest;
import com.studymatch.dto.LoginResponse;
import com.studymatch.dto.RegisterRequest;
import com.studymatch.model.Role;
import com.studymatch.repository.AppUserRepository;
import com.studymatch.security.AuthenticatedUser;
import com.studymatch.security.StudentAccess;
import com.studymatch.security.TokenService;
import com.studymatch.service.AuthService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private AppUserRepository userRepository;

    @MockitoBean
    private StudentAccess studentAccess;

    private UsernamePasswordAuthenticationToken authenticatedUser() {
        AuthenticatedUser user = new AuthenticatedUser(
                1L, "aluno1", Role.ALUNO, 10L, "Aluno Teste"
        );

        return new UsernamePasswordAuthenticationToken(
                user,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ALUNO"))
        );
    }

    @Test
    void loginShouldReturnToken() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenReturn(new LoginResponse(
                        "token-teste", Role.ALUNO, 1L, 10L, "Aluno Teste"
                ));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"aluno1\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-teste"))
                .andExpect(jsonPath("$.role").value("ALUNO"))
                .andExpect(jsonPath("$.studentId").value(10));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    void registerShouldReturnCreated() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(new LoginResponse(
                        "token-novo", Role.ALUNO, 2L, 20L, "Novo Aluno"
                ));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "studentNumber": "2026001",
                                  "name": "Novo Aluno",
                                  "email": "aluno@test.com",
                                  "course": "Engenharia Informática",
                                  "academicYear": 2,
                                  "password": "123456"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("token-novo"))
                .andExpect(jsonPath("$.displayName").value("Novo Aluno"));

        verify(authService).register(any(RegisterRequest.class));
    }

    @Test
    void meWithoutAuthenticationShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(authService);
    }

    @Test
    void meWithAuthenticationShouldReturnCurrentUser() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .with(authentication(authenticatedUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("aluno1"))
                .andExpect(jsonPath("$.role").value("ALUNO"))
                .andExpect(jsonPath("$.studentId").value(10));
    }

    @Test
    void logoutWithoutAuthenticationShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(authService);
    }

    @Test
    void logoutWithAuthenticationShouldReturnNoContent() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer token-teste")
                        .with(authentication(authenticatedUser())))
                .andExpect(status().isNoContent());

        verify(authService).logout("token-teste");
    }
}

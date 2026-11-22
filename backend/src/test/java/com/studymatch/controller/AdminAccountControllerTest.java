
package com.studymatch.controller;

import com.studymatch.config.SecurityConfig;
import com.studymatch.repository.AppUserRepository;
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

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminAccountController.class)
@Import(SecurityConfig.class)
class AdminAccountControllerTest {

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

    private UsernamePasswordAuthenticationToken adminAuth() {
        return new UsernamePasswordAuthenticationToken(
                "admin",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
    }

    private UsernamePasswordAuthenticationToken studentAuth() {
        return new UsernamePasswordAuthenticationToken(
                "aluno",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ALUNO"))
        );
    }

    @Test
    void setPasswordWithoutAuthenticationShouldReturn401() throws Exception {
        mockMvc.perform(put("/api/admin/students/10/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"123456\"}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(authService);
    }

    @Test
    void setPasswordAsStudentShouldReturn403() throws Exception {
        mockMvc.perform(put("/api/admin/students/10/password")
                        .with(authentication(studentAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"123456\"}"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(authService);
    }

    @Test
    void setPasswordAsAdminShouldReturn204() throws Exception {
        mockMvc.perform(put("/api/admin/students/10/password")
                        .with(authentication(adminAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"123456\"}"))
                .andExpect(status().isNoContent());

        verify(authService).setStudentPassword(10L, "123456");
    }
}

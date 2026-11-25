package com.studymatch.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import com.studymatch.config.SecurityConfig;
import com.studymatch.dto.ProfileResponse;
import com.studymatch.profile.LevelOrigin;
import com.studymatch.repository.AppUserRepository;
import com.studymatch.security.StudentAccess;
import com.studymatch.security.TokenService;
import com.studymatch.service.ProfileService;

/**
 * Testes HTTP do perfil de competências: GET /api/students/{id}/profile (Issue #113).
 * O cálculo em si está testado no ProfileCalculatorTest; aqui testa-se o pedido, a resposta e o acesso.
 */
@WebMvcTest(ProfileController.class)
@Import(SecurityConfig.class)
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProfileService profileService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private AppUserRepository userRepository;

    @MockitoBean
    private StudentAccess studentAccess;

    private static ProfileResponse profileOfAna() {
        return new ProfileResponse(5L, "Ana", 1, 5.0 / 6, true, List.of(
                new ProfileResponse.CompetencyProfile(
                        "PROG", "Programação", 3.8, LevelOrigin.MIXED, 5.0 / 6, 4.0, 3.0, List.of()),
                new ProfileResponse.CompetencyProfile(
                        "EQUIPA", "Trabalho em grupo", null, LevelOrigin.NO_DATA, 0.0, null, null, List.of())));
    }

    @Test
    void getProfile_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/students/5/profile"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(profileService);
    }

    @Test
    @WithMockUser(roles = "ALUNO")
    void getProfile_ownProfile_returns200WithCompetencies() throws Exception {
        when(studentAccess.canView(any(), eq(5L))).thenReturn(true);
        when(profileService.getProfile(5L)).thenReturn(profileOfAna());

        mockMvc.perform(get("/api/students/5/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value(5))
                .andExpect(jsonPath("$.completedCourseUnits").value(1))
                .andExpect(jsonPath("$.quizCompleted").value(true))
                .andExpect(jsonPath("$.competencies.length()").value(2))
                .andExpect(jsonPath("$.competencies[0].code").value("PROG"))
                .andExpect(jsonPath("$.competencies[0].level").value(3.8))
                .andExpect(jsonPath("$.competencies[0].origin").value("MIXED"));

        verify(profileService).getProfile(5L);
    }

    @Test
    @WithMockUser(roles = "ALUNO")
    void getProfile_competencyWithoutData_isNullNotZero() throws Exception {
        when(studentAccess.canView(any(), eq(5L))).thenReturn(true);
        when(profileService.getProfile(5L)).thenReturn(profileOfAna());

        // ADR-002 D8: "sem dados" chega ao frontend como null, nunca como 0.
        mockMvc.perform(get("/api/students/5/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.competencies[1].origin").value("NO_DATA"))
                .andExpect(jsonPath("$.competencies[1].level").doesNotExist());
    }

    @Test
    @WithMockUser(roles = "ALUNO")
    void getProfile_otherStudent_returns403() throws Exception {
        when(studentAccess.canView(any(), eq(6L))).thenReturn(false);

        mockMvc.perform(get("/api/students/6/profile"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(profileService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getProfile_studentNotFound_returns404() throws Exception {
        when(studentAccess.canView(any(), eq(99L))).thenReturn(true);
        when(profileService.getProfile(99L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Estudante não encontrado."));

        mockMvc.perform(get("/api/students/99/profile"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ALUNO")
    void getProfile_invalidId_returns403() throws Exception {
        // Um id que não é número não identifica nenhum aluno: o acesso é negado antes do controller.
        mockMvc.perform(get("/api/students/abc/profile"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(profileService);
    }
}

package com.studymatch.controller;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.studymatch.grouping.ImpossibleGroupConfigurationException;
import com.studymatch.repository.AppUserRepository;
import com.studymatch.security.StudentAccess;
import com.studymatch.security.TokenService;
import com.studymatch.service.GroupGenerationService;

/**
 * Erros e acessos dos grupos do administrador: /api/group-contexts (Issue #113).
 * Complementa o GroupContextControllerTest (casos de sucesso, sem segurança) com a segurança
 * ligada e os casos de erro que o frontend trata (groupApi.js: 400 e 404).
 */
@WebMvcTest(GroupContextController.class)
@Import(SecurityConfig.class)
class GroupContextErrorsTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GroupGenerationService groupGenerationService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private AppUserRepository userRepository;

    @MockitoBean
    private StudentAccess studentAccess;

    @Test
    void generate_withoutAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/group-contexts/1/generate"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(groupGenerationService);
    }

    @Test
    @WithMockUser(roles = "ALUNO")
    void generate_asStudent_returns403() throws Exception {
        mockMvc.perform(post("/api/group-contexts/1/generate"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(groupGenerationService);
    }

    @Test
    @WithMockUser(roles = "DOCENTE")
    void listContexts_asTeacher_returns403() throws Exception {
        // O docente tem os grupos da sua UC em /api/teacher/** (#106); este recurso é só do administrador.
        mockMvc.perform(get("/api/group-contexts"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(groupGenerationService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void generate_impossibleConfiguration_returns400WithMessage() throws Exception {
        // ADR-003 D1: com 7 alunos não há grupos de 4 ou 5. Tem de ser 400, nunca 500.
        when(groupGenerationService.generateGroups(1L)).thenThrow(
                new ImpossibleGroupConfigurationException("Com 7 alunos não é possível formar grupos de 4 ou 5."));

        mockMvc.perform(post("/api/group-contexts/1/generate"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Com 7 alunos não é possível formar grupos de 4 ou 5."));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void generate_contextWithoutStudents_returns400() throws Exception {
        when(groupGenerationService.generateGroups(1L)).thenThrow(
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "O contexto não tem estudantes associados."));

        mockMvc.perform(post("/api/group-contexts/1/generate"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void generate_unknownContext_returns404() throws Exception {
        when(groupGenerationService.generateGroups(99L)).thenThrow(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Contexto não encontrado."));

        mockMvc.perform(post("/api/group-contexts/99/generate"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getGroups_unknownContext_returns404() throws Exception {
        when(groupGenerationService.getGroups(99L)).thenThrow(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Contexto não encontrado."));

        mockMvc.perform(get("/api/group-contexts/99/groups"))
                .andExpect(status().isNotFound());
    }
}

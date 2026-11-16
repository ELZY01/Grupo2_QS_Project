package com.studymatch.controller;

import com.studymatch.config.SecurityConfig;
import com.studymatch.dto.CourseUnitResponse;
import com.studymatch.repository.AppUserRepository;
import com.studymatch.security.StudentAccess;
import com.studymatch.security.TokenService;
import com.studymatch.service.CourseUnitService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes da camada web (@WebMvcTest) para o CourseUnitController.
 * Valida o endpoint GET /api/course-units, serialização JSON e regras de autenticação.
 */
@WebMvcTest(CourseUnitController.class)
@Import(SecurityConfig.class) // Aplica as regras reais de segurança (exige utilizador autenticado)
class CourseUnitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Mock do serviço chamado pelo controller (não toca na base de dados)
    @MockitoBean
    private CourseUnitService courseUnitService;

    // Dependências necessárias para o arranque da SecurityConfig
    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private AppUserRepository userRepository;

    @MockitoBean
    private StudentAccess studentAccess;

    @Test
    void getAllCourseUnits_withoutAuth_returns401() throws Exception {
        // Sem autenticação: qualquer pedido a /api/course-units deve ser bloqueado com 401
        mockMvc.perform(get("/api/course-units"))
                .andExpect(status().isUnauthorized());

        // Garante que o serviço nem sequer foi chamado quando o pedido não está autenticado
        verifyNoInteractions(courseUnitService);
    }

    @Test
    @WithMockUser(roles = "ALUNO")
    void getAllCourseUnits_whenAuthenticatedAsStudent_returns200AndList() throws Exception {
        // Prepara dados simulados devolvidos pelo serviço
        List<CourseUnitResponse> units = List.of(
                new CourseUnitResponse(1L, "ES1", "Engenharia de Software I", 6, 2, 1),
                new CourseUnitResponse(2L, "BD", "Bases de Dados", 6, 2, 1)
        );
        when(courseUnitService.getAllCourseUnits()).thenReturn(units);

        // Caso válido: utilizador autenticado recebe 200 OK e a lista em JSON
        mockMvc.perform(get("/api/course-units"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].code").value("ES1"))
                .andExpect(jsonPath("$[0].name").value("Engenharia de Software I"))
                .andExpect(jsonPath("$[0].ects").value(6))
                .andExpect(jsonPath("$[0].academicYear").value(2))
                .andExpect(jsonPath("$[0].semester").value(1))
                .andExpect(jsonPath("$[1].code").value("BD"));

        verify(courseUnitService).getAllCourseUnits();
    }

    @Test
    @WithMockUser(roles = "DOCENTE")
    void getAllCourseUnits_whenEmpty_returns200AndEmptyArray() throws Exception {
        // Caso válido (lista vazia): deve devolver 200 OK e um array vazio []
        when(courseUnitService.getAllCourseUnits()).thenReturn(List.of());

        mockMvc.perform(get("/api/course-units"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(courseUnitService).getAllCourseUnits();
    }

    @Test
    @WithMockUser(roles = "ALUNO")
    void unknownSubRoute_whenAuthenticated_returns404() throws Exception {
        // Rota inexistente dentro de /api/course-units deve devolver 404 Not Found
        mockMvc.perform(get("/api/course-units/999"))
                .andExpect(status().isNotFound());
    }
}

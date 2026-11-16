package com.studymatch.controller;

import com.studymatch.config.SecurityConfig;
import com.studymatch.repository.AppUserRepository;
import com.studymatch.security.StudentAccess;
import com.studymatch.security.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes da camada web (@WebMvcTest) para o HealthController.
 * Isola apenas os endpoints deste controller, aplicando as regras reais de segurança.
 */
@WebMvcTest(HealthController.class)
@Import(SecurityConfig.class) // Importa a configuração de segurança (rotas públicas vs protegidas)
class HealthControllerTest {

    // Simula pedidos HTTP (GET, POST, etc.) sem precisar de arrancar um servidor web real
    @Autowired
    private MockMvc mockMvc;

    // Dependências exigidas pela SecurityConfig que são simuladas (mocks) neste teste isolado
    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private AppUserRepository userRepository;

    @MockitoBean
    private StudentAccess studentAccess;

    @Test
    void health_returnsOkAndServiceStatus_evenWithoutAuth() throws Exception {
        // Caso válido: GET /api/health é público, deve devolver 200 OK e o JSON esperado
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("studymatch-backend"));
    }

    @Test
    void unknownEndpoint_withoutAuth_returns401() throws Exception {
        // Sem permissão/autenticação: qualquer outra rota protegida sem token deve devolver 401 Unauthorized
        mockMvc.perform(get("/api/health/inexistente"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void postOnHealth_withoutAuth_returns401() throws Exception {
        // Sem permissão/autenticação: método HTTP não público (POST) sem token deve devolver 401 Unauthorized
        mockMvc.perform(post("/api/health"))
                .andExpect(status().isUnauthorized());
    }
}
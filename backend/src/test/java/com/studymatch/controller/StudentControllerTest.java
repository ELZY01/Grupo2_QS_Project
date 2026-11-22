
package com.studymatch.controller;

import com.studymatch.model.Role;
import com.studymatch.model.Student;
import com.studymatch.repository.AppUserRepository;
import com.studymatch.security.AuthenticatedUser;
import com.studymatch.config.SecurityConfig;
import com.studymatch.security.StudentAccess;
import com.studymatch.security.TokenService;
import com.studymatch.service.StudentService;

import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StudentController.class)
@Import(SecurityConfig.class)
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StudentService studentService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private AppUserRepository userRepository;

    @MockitoBean
    private StudentAccess studentAccess;

    private static final String VALID_STUDENT = """
            {
                "studentNumber": "2024001",
                "name": "João Silva",
                "email": "joao@exemplo.pt",
                "course": "Engenharia Informática",
                "academicYear": 2
            }
            """;

    @BeforeEach
    void configureStudentAccess() {
        when(studentAccess.canEdit(any(AuthenticatedUser.class), eq(1L)))
                .thenReturn(true);
    }

    private UsernamePasswordAuthenticationToken adminAuthentication() {
        AuthenticatedUser admin = new AuthenticatedUser(
                1L,
                "admin",
                Role.ADMIN,
                null,
                "Admin"
        );

        return new UsernamePasswordAuthenticationToken(
                admin,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
    }

    // =========================
    // POST /api/students
    // =========================

    @Test
    void deveCriarEstudanteComDadosValidos() throws Exception {
        when(studentService.createStudent(any(Student.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/api/students")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_STUDENT))
                .andExpect(status().isCreated());
    }

    @Test
    void deveRejeitarNomeVazio() throws Exception {
        String json = """
                {
                    "studentNumber": "2024001",
                    "name": "",
                    "email": "joao@exemplo.pt",
                    "course": "Engenharia Informática",
                    "academicYear": 2
                }
                """;

        mockMvc.perform(post("/api/students")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRejeitarEmailInvalido() throws Exception {
        String json = """
                {
                    "studentNumber": "2024001",
                    "name": "João Silva",
                    "email": "email-invalido",
                    "course": "Engenharia Informática",
                    "academicYear": 2
                }
                """;

        mockMvc.perform(post("/api/students")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRejeitarNumeroEstudanteVazio() throws Exception {
        String json = """
                {
                    "studentNumber": "",
                    "name": "João Silva",
                    "email": "joao@exemplo.pt",
                    "course": "Engenharia Informática",
                    "academicYear": 2
                }
                """;

        mockMvc.perform(post("/api/students")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRejeitarCursoVazio() throws Exception {
        String json = """
                {
                    "studentNumber": "2024001",
                    "name": "João Silva",
                    "email": "joao@exemplo.pt",
                    "course": "",
                    "academicYear": 2
                }
                """;

        mockMvc.perform(post("/api/students")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRejeitarAnoAcademicoInferiorAUm() throws Exception {
        String json = """
                {
                    "studentNumber": "2024001",
                    "name": "João Silva",
                    "email": "joao@exemplo.pt",
                    "course": "Engenharia Informática",
                    "academicYear": 0
                }
                """;

        mockMvc.perform(post("/api/students")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    // =========================
    // PUT /api/students/{id}
    // =========================

    @Test
    void deveRejeitarAtualizacaoComNomeVazio() throws Exception {
        String json = """
                {
                    "studentNumber": "2024001",
                    "name": "",
                    "email": "joao@exemplo.pt",
                    "course": "Engenharia Informática",
                    "academicYear": 2
                }
                """;

        mockMvc.perform(put("/api/students/1")
                        .with(authentication(adminAuthentication()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRejeitarAtualizacaoComEmailInvalido() throws Exception {
        String json = """
                {
                    "studentNumber": "2024001",
                    "name": "João Silva",
                    "email": "email-invalido",
                    "course": "Engenharia Informática",
                    "academicYear": 2
                }
                """;

        mockMvc.perform(put("/api/students/1")
                        .with(authentication(adminAuthentication()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRejeitarAtualizacaoComAnoAcademicoInvalido() throws Exception {
        String json = """
                {
                    "studentNumber": "2024001",
                    "name": "João Silva",
                    "email": "joao@exemplo.pt",
                    "course": "Engenharia Informática",
                    "academicYear": 0
                }
                """;

        mockMvc.perform(put("/api/students/1")
                        .with(authentication(adminAuthentication()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveAtualizarEstudanteComDadosValidos() throws Exception {
        when(studentService.updateStudent(eq(1L), any(Student.class)))
                .thenAnswer(invocation -> invocation.getArgument(1));

        mockMvc.perform(put("/api/students/1")
                        .with(authentication(adminAuthentication()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_STUDENT))
                .andExpect(status().isOk());
    }
}

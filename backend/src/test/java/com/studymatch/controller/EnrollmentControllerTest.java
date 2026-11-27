package com.studymatch.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import com.studymatch.config.SecurityConfig;
import com.studymatch.dto.EnrollmentRequest;
import com.studymatch.dto.EnrollmentResponse;
import com.studymatch.model.Enrollment.EnrollmentStatus;
import com.studymatch.repository.AppUserRepository;
import com.studymatch.security.StudentAccess;
import com.studymatch.security.TokenService;
import com.studymatch.service.EnrollmentService;

/**
 * Testes HTTP do histórico académico: /api/students/{id}/enrollments (Issue #113).
 * As regras das notas estão no EnrollmentServiceTest; aqui testa-se o pedido, a resposta e o acesso.
 */
@WebMvcTest(EnrollmentController.class)
@Import(SecurityConfig.class)
class EnrollmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EnrollmentService enrollmentService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private AppUserRepository userRepository;

    @MockitoBean
    private StudentAccess studentAccess;

    private static final String APPROVED_17 = """
            {
              "courseUnitId": 1,
              "schoolYear": 2026,
              "semester": 1,
              "grade": 17,
              "status": "APROVADO"
            }
            """;

    private static EnrollmentResponse approved(long id, int attempt, int grade) {
        return new EnrollmentResponse(id, 5L, 1L, 2026, 1, attempt, grade, EnrollmentStatus.APROVADO);
    }

    // =========================================================================
    // POST /api/students/{id}/enrollments (só o administrador lança notas)
    // =========================================================================

    @Test
    void createEnrollment_withoutAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/students/5/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(APPROVED_17))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(enrollmentService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createEnrollment_asAdmin_returns201WithAttempt() throws Exception {
        when(enrollmentService.createEnrollment(eq(5L), any(EnrollmentRequest.class)))
                .thenReturn(approved(10L, 1, 17));

        mockMvc.perform(post("/api/students/5/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(APPROVED_17))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.attemptNumber").value(1))
                .andExpect(jsonPath("$.grade").value(17))
                .andExpect(jsonPath("$.status").value("APROVADO"));

        verify(enrollmentService).createEnrollment(eq(5L), any(EnrollmentRequest.class));
    }

    @Test
    @WithMockUser(roles = "ALUNO")
    void createEnrollment_asStudent_returns403() throws Exception {
        // O aluno não lança as próprias notas (Sprint 2, PR #64): só o administrador.
        mockMvc.perform(post("/api/students/5/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(APPROVED_17))
                .andExpect(status().isForbidden());

        verifyNoInteractions(enrollmentService);
    }

    @Test
    @WithMockUser(roles = "DOCENTE")
    void createEnrollment_asTeacher_returns403() throws Exception {
        mockMvc.perform(post("/api/students/5/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(APPROVED_17))
                .andExpect(status().isForbidden());

        verifyNoInteractions(enrollmentService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createEnrollment_invalidGrade_returns400() throws Exception {
        when(enrollmentService.createEnrollment(eq(5L), any(EnrollmentRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "A nota deve estar entre 0 e 20."));

        mockMvc.perform(post("/api/students/5/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(APPROVED_17.replace("17", "21")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createEnrollment_unknownStudent_returns404() throws Exception {
        when(enrollmentService.createEnrollment(eq(99L), any(EnrollmentRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Estudante não encontrado."));

        mockMvc.perform(post("/api/students/99/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(APPROVED_17))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createEnrollment_malformedJson_returns400() throws Exception {
        mockMvc.perform(post("/api/students/5/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ isto não é JSON"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(enrollmentService);
    }

    // =========================================================================
    // GET /api/students/{id}/enrollments
    // =========================================================================

    @Test
    void getEnrollments_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/students/5/enrollments"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(enrollmentService);
    }

    @Test
    @WithMockUser(roles = "ALUNO")
    void getEnrollments_ownHistory_returns200WithAllAttempts() throws Exception {
        when(studentAccess.canView(any(), eq(5L))).thenReturn(true);
        when(enrollmentService.getStudentEnrollments(5L)).thenReturn(List.of(
                new EnrollmentResponse(1L, 5L, 1L, 2025, 1, 1, 8, EnrollmentStatus.REPROVADO),
                approved(2L, 2, 14)));

        mockMvc.perform(get("/api/students/5/enrollments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].status").value("REPROVADO"))
                .andExpect(jsonPath("$[1].attemptNumber").value(2))
                .andExpect(jsonPath("$[1].grade").value(14));
    }

    @Test
    @WithMockUser(roles = "ALUNO")
    void getEnrollments_otherStudent_returns403() throws Exception {
        when(studentAccess.canView(any(), eq(6L))).thenReturn(false);

        mockMvc.perform(get("/api/students/6/enrollments"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(enrollmentService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getEnrollments_unknownStudent_returns404() throws Exception {
        when(studentAccess.canView(any(), eq(99L))).thenReturn(true);
        when(enrollmentService.getStudentEnrollments(99L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Estudante não encontrado."));

        mockMvc.perform(get("/api/students/99/enrollments"))
                .andExpect(status().isNotFound());
    }
}

package com.studymatch.controller;

import com.studymatch.config.SecurityConfig;
import com.studymatch.dto.CourseUnitResponse;
import com.studymatch.dto.StudentSummary;
import com.studymatch.dto.TeacherCourseUnitResponse;
import com.studymatch.model.Student;
import com.studymatch.repository.AppUserRepository;
import com.studymatch.security.AuthenticatedUser;
import com.studymatch.security.StudentAccess;
import com.studymatch.security.TokenService;
import com.studymatch.service.TeacherService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes da camada web (@WebMvcTest) para o TeacherController.
 * Valida o acesso restrito a docentes, listagem de UCs do docente e alunos inscritos.
 */
@WebMvcTest(TeacherController.class)
@Import(SecurityConfig.class)
class TeacherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TeacherService teacherService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private AppUserRepository userRepository;

    @MockitoBean
    private StudentAccess studentAccess;

    /**
     * Cria um token de autenticação simulando um docente autenticado com ID 1L.
     */
    private UsernamePasswordAuthenticationToken teacherAuth() {
        AuthenticatedUser user = mock(AuthenticatedUser.class);
        when(user.id()).thenReturn(1L);
        return new UsernamePasswordAuthenticationToken(
                user,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_DOCENTE"))
        );
    }

    // =========================================================================
    // GET /api/teacher/course-units
    // =========================================================================

    @Test
    void myCourseUnits_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/teacher/course-units"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(teacherService);
    }

    @Test
    @WithMockUser(roles = "ALUNO")
    void myCourseUnits_whenAuthenticatedAsStudent_returns403() throws Exception {
        mockMvc.perform(get("/api/teacher/course-units"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(teacherService);
    }

    @Test
    void myCourseUnits_whenAuthenticatedAsTeacher_returns200AndList() throws Exception {
        CourseUnitResponse cu = new CourseUnitResponse(1L, "ES1", "Engenharia de Software I", 6, 2, 1);
        TeacherCourseUnitResponse responseItem = new TeacherCourseUnitResponse(cu, 15L);

        when(teacherService.myCourseUnits(1L)).thenReturn(List.of(responseItem));

        mockMvc.perform(get("/api/teacher/course-units")
                        .with(authentication(teacherAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].courseUnit.code").value("ES1"))
                .andExpect(jsonPath("$[0].courseUnit.name").value("Engenharia de Software I"))
                .andExpect(jsonPath("$[0].studentCount").value(15));

        verify(teacherService).myCourseUnits(1L);
    }

    // =========================================================================
    // GET /api/teacher/course-units/{courseUnitId}/students
    // =========================================================================

    @Test
    void students_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/teacher/course-units/10/students"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(teacherService);
    }

    @Test
    @WithMockUser(roles = "ALUNO")
    void students_whenAuthenticatedAsStudent_returns403() throws Exception {
        mockMvc.perform(get("/api/teacher/course-units/10/students"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(teacherService);
    }

    @Test
    void students_whenTeacherNotAssignedToCourseUnit_returns403() throws Exception {
        when(teacherService.studentsOf(eq(1L), anyLong()))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Esta UC não te está atribuída."));

        mockMvc.perform(get("/api/teacher/course-units/10/students")
                        .with(authentication(teacherAuth())))
                .andExpect(status().isForbidden());
    }

    @Test
    void students_whenCourseUnitNotFound_returns404() throws Exception {
        when(teacherService.studentsOf(eq(1L), anyLong()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Unidade curricular não encontrada."));

        mockMvc.perform(get("/api/teacher/course-units/999/students")
                        .with(authentication(teacherAuth())))
                .andExpect(status().isNotFound());
    }

    @Test
    void students_whenValidAssignment_returns200AndStudentSummaryList() throws Exception {
        Student studentEntity = mock(Student.class);
        when(studentEntity.getId()).thenReturn(5L);
        when(studentEntity.getName()).thenReturn("Bruna Silva");

        StudentSummary summary = StudentSummary.from(studentEntity);

        when(teacherService.studentsOf(eq(1L), eq(10L))).thenReturn(List.of(summary));

        mockMvc.perform(get("/api/teacher/course-units/10/students")
                        .with(authentication(teacherAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(teacherService).studentsOf(eq(1L), eq(10L));
    }
}
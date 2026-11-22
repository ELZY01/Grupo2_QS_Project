
package com.studymatch.controller;

import com.studymatch.config.SecurityConfig;
import com.studymatch.dto.CourseUnitAssignmentsResponse;
import com.studymatch.dto.CourseUnitResponse;
import com.studymatch.dto.CreateTeacherRequest;
import com.studymatch.dto.IdRequest;
import com.studymatch.repository.AppUserRepository;
import com.studymatch.security.StudentAccess;
import com.studymatch.security.TokenService;
import com.studymatch.service.CourseUnitAssignmentService;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminAssignmentController.class)
@Import(SecurityConfig.class)
class AdminAssignmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CourseUnitAssignmentService service;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private AppUserRepository userRepository;

    @MockitoBean
    private StudentAccess studentAccess;

    private UsernamePasswordAuthenticationToken adminAuth() {
        return new UsernamePasswordAuthenticationToken(
                "admin", null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
    }

    private UsernamePasswordAuthenticationToken studentAuth() {
        return new UsernamePasswordAuthenticationToken(
                "aluno", null,
                List.of(new SimpleGrantedAuthority("ROLE_ALUNO"))
        );
    }

    private CourseUnitAssignmentsResponse emptyAssignments() {
        return new CourseUnitAssignmentsResponse(
                new CourseUnitResponse(1L, "ES1", "Engenharia de Software I", 6, 2, 1),
                List.of(),
                List.of()
        );
    }

    @Test
    void listTeachersAsAdminShouldReturn200() throws Exception {
        when(service.listTeachers()).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/teachers")
                        .with(authentication(adminAuth())))
                .andExpect(status().isOk());

        verify(service).listTeachers();
    }

    @Test
    void createTeacherAsAdminShouldReturn201() throws Exception {
        when(service.createTeacher(any(CreateTeacherRequest.class)))
                .thenReturn(null);

        mockMvc.perform(post("/api/admin/teachers")
                        .with(authentication(adminAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "professor1",
                                  "displayName": "Professor Teste",
                                  "password": "123456"
                                }
                                """))
                .andExpect(status().isCreated());

        verify(service).createTeacher(any(CreateTeacherRequest.class));
    }

    @Test
    void getAssignmentsAsAdminShouldReturn200() throws Exception {
        when(service.getAssignments(1L)).thenReturn(emptyAssignments());

        mockMvc.perform(get("/api/admin/course-units/1/assignments")
                        .with(authentication(adminAuth())))
                .andExpect(status().isOk());

        verify(service).getAssignments(1L);
    }

    @Test
    void assignTeacherAsAdminShouldReturn200() throws Exception {
        when(service.assignTeacher(1L, 2L)).thenReturn(emptyAssignments());

        mockMvc.perform(post("/api/admin/course-units/1/teachers")
                        .with(authentication(adminAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":2}"))
                .andExpect(status().isOk());

        verify(service).assignTeacher(1L, 2L);
    }

    @Test
    void removeTeacherAsAdminShouldReturn200() throws Exception {
        when(service.removeTeacher(1L, 2L)).thenReturn(emptyAssignments());

        mockMvc.perform(delete("/api/admin/course-units/1/teachers/2")
                        .with(authentication(adminAuth())))
                .andExpect(status().isOk());

        verify(service).removeTeacher(1L, 2L);
    }

    @Test
    void enrollStudentAsAdminShouldReturn200() throws Exception {
        when(service.enrollStudent(1L, 3L)).thenReturn(emptyAssignments());

        mockMvc.perform(post("/api/admin/course-units/1/students")
                        .with(authentication(adminAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":3}"))
                .andExpect(status().isOk());

        verify(service).enrollStudent(1L, 3L);
    }

    @Test
    void removeStudentAsAdminShouldReturn200() throws Exception {
        when(service.removeStudent(1L, 3L)).thenReturn(emptyAssignments());

        mockMvc.perform(delete("/api/admin/course-units/1/students/3")
                        .with(authentication(adminAuth())))
                .andExpect(status().isOk());

        verify(service).removeStudent(1L, 3L);
    }

    @Test
    void listTeachersWithoutAuthenticationShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/admin/teachers"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(service);
    }

    @Test
    void createTeacherWithoutAuthenticationShouldReturn401() throws Exception {
        mockMvc.perform(post("/api/admin/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"prof\",\"displayName\":\"Professor\",\"password\":\"123456\"}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(service);
    }

    @Test
    void getAssignmentsWithoutAuthenticationShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/admin/course-units/1/assignments"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(service);
    }

    @Test
    void assignTeacherWithoutAuthenticationShouldReturn401() throws Exception {
        mockMvc.perform(post("/api/admin/course-units/1/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":2}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(service);
    }

    @Test
    void removeTeacherWithoutAuthenticationShouldReturn401() throws Exception {
        mockMvc.perform(delete("/api/admin/course-units/1/teachers/2"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(service);
    }

    @Test
    void enrollStudentWithoutAuthenticationShouldReturn401() throws Exception {
        mockMvc.perform(post("/api/admin/course-units/1/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":3}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(service);
    }

    @Test
    void removeStudentWithoutAuthenticationShouldReturn401() throws Exception {
        mockMvc.perform(delete("/api/admin/course-units/1/students/3"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(service);
    }

    @Test
    void listTeachersAsStudentShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/admin/teachers")
                        .with(authentication(studentAuth())))
                .andExpect(status().isForbidden());

        verifyNoInteractions(service);
    }
}

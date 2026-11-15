package com.studymatch.controller;

import com.studymatch.dto.CourseUnitAssignmentsResponse;
import com.studymatch.dto.CreateTeacherRequest;
import com.studymatch.dto.IdRequest;
import com.studymatch.dto.TeacherResponse;
import com.studymatch.service.CourseUnitAssignmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Atribuições de docentes e alunos às UCs (só ADMIN, ver SecurityConfig).
 */
@RestController
@RequestMapping("/api/admin")
public class AdminAssignmentController {

    private final CourseUnitAssignmentService service;

    public AdminAssignmentController(CourseUnitAssignmentService service) {
        this.service = service;
    }

    /** GET /api/admin/teachers */
    @GetMapping("/teachers")
    public List<TeacherResponse> listTeachers() {
        return service.listTeachers();
    }

    /** POST /api/admin/teachers — cria a conta de um docente (201). */
    @PostMapping("/teachers")
    public ResponseEntity<TeacherResponse> createTeacher(@RequestBody CreateTeacherRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createTeacher(request));
    }

    /** GET /api/admin/course-units/{id}/assignments — docentes e alunos inscritos. */
    @GetMapping("/course-units/{courseUnitId}/assignments")
    public CourseUnitAssignmentsResponse getAssignments(@PathVariable Long courseUnitId) {
        return service.getAssignments(courseUnitId);
    }

    /** POST /api/admin/course-units/{id}/teachers {id} — R2. */
    @PostMapping("/course-units/{courseUnitId}/teachers")
    public CourseUnitAssignmentsResponse assignTeacher(@PathVariable Long courseUnitId,
                                                       @RequestBody IdRequest request) {
        return service.assignTeacher(courseUnitId, request == null ? null : request.id());
    }

    /** DELETE /api/admin/course-units/{id}/teachers/{teacherId} — R2. */
    @DeleteMapping("/course-units/{courseUnitId}/teachers/{teacherId}")
    public CourseUnitAssignmentsResponse removeTeacher(@PathVariable Long courseUnitId,
                                                       @PathVariable Long teacherId) {
        return service.removeTeacher(courseUnitId, teacherId);
    }

    /** POST /api/admin/course-units/{id}/students {id} — R1. */
    @PostMapping("/course-units/{courseUnitId}/students")
    public CourseUnitAssignmentsResponse enrollStudent(@PathVariable Long courseUnitId,
                                                       @RequestBody IdRequest request) {
        return service.enrollStudent(courseUnitId, request == null ? null : request.id());
    }

    /** DELETE /api/admin/course-units/{id}/students/{studentId} — R1. */
    @DeleteMapping("/course-units/{courseUnitId}/students/{studentId}")
    public CourseUnitAssignmentsResponse removeStudent(@PathVariable Long courseUnitId,
                                                       @PathVariable Long studentId) {
        return service.removeStudent(courseUnitId, studentId);
    }
}

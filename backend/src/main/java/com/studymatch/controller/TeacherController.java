package com.studymatch.controller;

import com.studymatch.dto.StudentSummary;
import com.studymatch.dto.TeacherCourseUnitResponse;
import com.studymatch.security.AuthenticatedUser;
import com.studymatch.service.TeacherService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Área do docente (só DOCENTE, ver SecurityConfig).
 */
@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    /** GET /api/teacher/course-units — as UCs do docente com sessão iniciada. */
    @GetMapping("/course-units")
    public List<TeacherCourseUnitResponse> myCourseUnits(@AuthenticationPrincipal AuthenticatedUser user) {
        return teacherService.myCourseUnits(user.id());
    }

    /** GET /api/teacher/course-units/{id}/students — alunos inscritos (403 se a UC não for do docente). */
    @GetMapping("/course-units/{courseUnitId}/students")
    public List<StudentSummary> students(@AuthenticationPrincipal AuthenticatedUser user,
                                         @PathVariable Long courseUnitId) {
        return teacherService.studentsOf(user.id(), courseUnitId);
    }
}

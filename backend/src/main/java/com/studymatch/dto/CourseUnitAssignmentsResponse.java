package com.studymatch.dto;

import java.util.List;

/**
 * Docentes e alunos inscritos numa unidade curricular.
 */
public record CourseUnitAssignmentsResponse(
        CourseUnitResponse courseUnit,
        List<TeacherResponse> teachers,
        List<StudentSummary> students
) {
}

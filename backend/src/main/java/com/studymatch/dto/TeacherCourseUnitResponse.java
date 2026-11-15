package com.studymatch.dto;

/**
 * UC atribuída ao docente, com o nº de alunos inscritos.
 */
public record TeacherCourseUnitResponse(CourseUnitResponse courseUnit, long studentCount) {
}

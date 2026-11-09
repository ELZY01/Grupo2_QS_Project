package com.studymatch.dto;

import com.studymatch.model.Student;

/**
 * Dados básicos de um aluno numa lista (sem notas).
 */
public record StudentSummary(Long id, String studentNumber, String name, String email, String course,
                             Integer academicYear) {

    public static StudentSummary from(Student student) {
        return new StudentSummary(student.getId(), student.getStudentNumber(), student.getName(),
                student.getEmail(), student.getCourse(), student.getAcademicYear());
    }
}

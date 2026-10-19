
package com.studymatch.dto;

import com.studymatch.model.Enrollment;
import com.studymatch.model.Enrollment.EnrollmentStatus;

/**
 * Representa os dados de uma inscrição
 * devolvidos pela API ao frontend.
 *
 * Evita devolver diretamente as entidades
 * do Hibernate e as suas relações.
 */
public record EnrollmentResponse(
        Long id,
        Long studentId,
        Long courseUnitId,
        Integer schoolYear,
        Integer semester,
        Integer attemptNumber,
        Integer grade,
        EnrollmentStatus status
) {

    /**
     * Converte uma entidade Enrollment
     * numa resposta JSON.
     */
    public static EnrollmentResponse from(Enrollment enrollment) {
        return new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getStudent().getId(),
                enrollment.getCourseUnit().getId(),
                enrollment.getSchoolYear(),
                enrollment.getSemester(),
                enrollment.getAttemptNumber(),
                enrollment.getGrade(),
                enrollment.getStatus()
        );
    }
}

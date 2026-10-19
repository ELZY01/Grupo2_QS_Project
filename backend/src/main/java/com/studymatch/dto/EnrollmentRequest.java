
package com.studymatch.dto;

import com.studymatch.model.Enrollment.EnrollmentStatus;

/**
 * Representa os dados enviados pelo frontend
 * para criar uma inscrição académica.
 *
 * O ID do estudante vem do URL da API.
 * O número da tentativa será calculado automaticamente.
 */
public record EnrollmentRequest(
        Long courseUnitId,
        Integer schoolYear,
        Integer semester,
        Integer grade,
        EnrollmentStatus status
) {
}

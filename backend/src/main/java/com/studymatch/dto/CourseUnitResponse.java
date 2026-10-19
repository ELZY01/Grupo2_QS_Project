package com.studymatch.dto;

import com.studymatch.model.CourseUnit;

/**
 * Representa uma unidade curricular devolvida pela API.
 *
 * Usado pelo frontend para preencher a escolha da UC
 * no registo de inscrições e para mostrar o nome da UC no histórico.
 */
public record CourseUnitResponse(
        Long id,
        String code,
        String name,
        Integer ects,
        Integer academicYear,
        Integer semester
) {

    /**
     * Converte uma entidade CourseUnit numa resposta JSON.
     */
    public static CourseUnitResponse from(CourseUnit courseUnit) {
        return new CourseUnitResponse(
                courseUnit.getId(),
                courseUnit.getCode(),
                courseUnit.getName(),
                courseUnit.getEcts(),
                courseUnit.getAcademicYear(),
                courseUnit.getSemester()
        );
    }
}

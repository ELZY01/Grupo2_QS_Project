package com.studymatch.dto;

import java.time.LocalDateTime;

/**
 * Contexto de agrupamento de uma UC, visto na área do docente (Issue #106, US10).
 *
 * @param id               ID do contexto
 * @param courseUnitId     UC a que pertence
 * @param name             nome da atividade
 * @param studentCount     nº de alunos elegíveis
 * @param createdAt        data de criação
 * @param generatedAt      data da última geração automática, ou {@code null}
 * @param generationNumber geração mais recente com grupos, ou {@code null} se ainda não há grupos
 */
public record TeacherGroupContextResponse(
        Long id,
        Long courseUnitId,
        String name,
        int studentCount,
        LocalDateTime createdAt,
        LocalDateTime generatedAt,
        Integer generationNumber) {
}

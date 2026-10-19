package com.studymatch.dto;

import com.studymatch.profile.Contribution;
import com.studymatch.profile.LevelOrigin;

import java.util.List;

/**
 * Resposta de GET /api/students/{studentId}/profile.
 *
 * @param studentId            ID do estudante
 * @param studentName          nome do estudante
 * @param completedCourseUnits nº de UCs diferentes aprovadas (o n da fórmula do ADR-002)
 * @param quizWeight           peso global do quiz: max(0, 1 − n/6)
 * @param quizCompleted        true se o estudante já respondeu ao quiz inicial
 * @param competencies         um nível por competência
 */
public record ProfileResponse(
        Long studentId,
        String studentName,
        int completedCourseUnits,
        double quizWeight,
        boolean quizCompleted,
        List<CompetencyProfile> competencies) {

    /**
     * Nível de uma competência e a sua explicação (US16).
     *
     * @param code          código da competência (ex.: PROG)
     * @param name          nome da competência (ex.: Programação)
     * @param level         nível de 1 a 5, ou null quando origin = NO_DATA
     * @param origin        QUIZ, HISTORY, MIXED ou NO_DATA
     * @param quizWeight    peso efetivo do quiz neste nível (0 a 1)
     * @param quizLevel     resultado do quiz nesta competência, ou null
     * @param historyLevel  nível vindo das notas, ou null
     * @param contributions UCs que contribuíram para o nível histórico
     */
    public record CompetencyProfile(
            String code,
            String name,
            Double level,
            LevelOrigin origin,
            double quizWeight,
            Double quizLevel,
            Double historyLevel,
            List<Contribution> contributions) {
    }
}

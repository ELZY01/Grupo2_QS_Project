package com.studymatch.profile;

import java.util.List;
import java.util.Optional;

/**
 * Perfil de competências de um estudante.
 *
 * @param completedCourseUnits número de UCs diferentes já aprovadas (o {@code n} da fórmula)
 * @param quizWeight           peso global do quiz: {@code max(0, 1 − n/N)}
 * @param competencies         um nível por competência, pela ordem pedida
 */
public record StudentProfile(int completedCourseUnits, double quizWeight, List<CompetencyLevel> competencies) {

    public StudentProfile {
        competencies = List.copyOf(competencies);
    }

    public Optional<CompetencyLevel> competency(String competencyCode) {
        return competencies.stream()
                .filter(c -> c.competencyCode().equals(competencyCode))
                .findFirst();
    }
}

package com.studymatch.profile;

import java.util.List;

/**
 * Nível calculado de uma competência, com tudo o que é preciso para o explicar.
 *
 * @param competencyCode código da competência
 * @param level          nível final de 1 a 5, ou {@code null} quando {@code origin == NO_DATA}
 * @param origin         de onde vem o nível
 * @param quizWeight     peso efetivo do quiz neste nível (0 a 1)
 * @param quizLevel      resultado do quiz nesta competência, ou {@code null}
 * @param historyLevel   nível vindo das notas, ou {@code null}
 * @param contributions  UCs que contribuíram para o nível histórico
 */
public record CompetencyLevel(
        String competencyCode,
        Double level,
        LevelOrigin origin,
        double quizWeight,
        Double quizLevel,
        Double historyLevel,
        List<Contribution> contributions) {

    public CompetencyLevel {
        contributions = List.copyOf(contributions);
    }

    public boolean hasData() {
        return origin != LevelOrigin.NO_DATA;
    }
}

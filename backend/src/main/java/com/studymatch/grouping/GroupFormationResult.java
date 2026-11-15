package com.studymatch.grouping;

import java.util.List;
import java.util.Objects;

/**
 * Resultado de uma geração de grupos (Issue #73, ADR-003 D6).
 *
 * @param groups        grupos formados, pela ordem do seu número
 * @param maxAverageGap diferença entre a maior e a menor média de grupo (RN24: alvo ≤ 0,5)
 */
public record GroupFormationResult(List<FormedGroup> groups, double maxAverageGap) {

    public GroupFormationResult {
        Objects.requireNonNull(groups, "Os grupos são obrigatórios.");
        groups = List.copyOf(groups);
    }

    /** Nº total de alunos distribuídos. */
    public int studentCount() {
        return groups.stream().mapToInt(FormedGroup::size).sum();
    }
}

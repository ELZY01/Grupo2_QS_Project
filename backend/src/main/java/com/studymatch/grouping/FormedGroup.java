package com.studymatch.grouping;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Um grupo formado pelo algoritmo, com a sua explicação (Issue #73, ADR-003 D6).
 *
 * @param number          número do grupo (1, 2, ...)
 * @param members         alunos do grupo (4 ou 5)
 * @param average         média do grupo: média das médias dos membros
 * @param gapToOthers     média do grupo − média dos outros grupos
 * @param strongestByArea membro mais forte em cada competência; uma área sem dados no grupo fica de fora
 */
public record FormedGroup(
        int number,
        List<GroupCandidate> members,
        double average,
        double gapToOthers,
        Map<String, GroupCandidate> strongestByArea) {

    public FormedGroup {
        if (number < 1) {
            throw new IllegalArgumentException("O número do grupo começa em 1.");
        }
        Objects.requireNonNull(members, "Os membros do grupo são obrigatórios.");
        Objects.requireNonNull(strongestByArea, "O mais forte por área é obrigatório.");
        members = List.copyOf(members);
        strongestByArea = Collections.unmodifiableMap(new LinkedHashMap<>(strongestByArea));
    }

    public int size() {
        return members.size();
    }
}

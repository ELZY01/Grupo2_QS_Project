package com.studymatch.grouping;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Estatísticas de um grupo numa competência (Issue #106, R6).
 *
 * @param competencyCode  código da competência (ex.: PROG)
 * @param average         média dos membros com dados, ou {@code null} se ninguém tem dados
 * @param min             nível mais baixo no grupo, ou {@code null}
 * @param max             nível mais alto no grupo, ou {@code null}
 * @param membersWithData nº de membros com nível nesta competência
 * @param strongMembers   nº de membros fortes (nível ≥ 3,5, ADR-003 D4)
 * @param distribution    nº de membros por nível arredondado, de 1 a 5 (as 5 chaves estão sempre presentes)
 */
public record AreaStatistics(
        String competencyCode,
        Double average,
        Double min,
        Double max,
        int membersWithData,
        int strongMembers,
        Map<Integer, Integer> distribution) {

    public AreaStatistics {
        Objects.requireNonNull(competencyCode, "O código da competência é obrigatório.");
        Objects.requireNonNull(distribution, "A distribuição é obrigatória.");
        distribution = Collections.unmodifiableMap(new LinkedHashMap<>(distribution));
    }

    /** {@code true} se ninguém do grupo é forte nesta área (lacuna). */
    public boolean isGap() {
        return strongMembers == 0;
    }

    /** {@code true} se nenhum membro tem dados nesta área. */
    public boolean hasNoData() {
        return membersWithData == 0;
    }
}

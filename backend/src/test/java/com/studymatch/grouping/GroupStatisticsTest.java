package com.studymatch.grouping;

import static com.studymatch.grouping.SnakeGroupFormationTest.levels;
import static com.studymatch.grouping.SnakeGroupFormationTest.student;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Testes das estatísticas por grupo da área do docente (Issue #106, R6).
 */
@DisplayName("Estatísticas por grupo")
class GroupStatisticsTest {

    private static final double EPS = 1e-9;
    private static final List<String> AREAS = List.of("ORG", "TEMPO", "PROG", "EQUIPA");

    private static GroupCandidate withLevels(int id, Double org, Double tempo, Double prog, Double equipa) {
        return new GroupCandidate((long) id, String.format("A%03d", id), "Estudante " + id,
                levels(org, tempo, prog, equipa));
    }

    @Test
    @DisplayName("Conta os membros e calcula a média do grupo")
    void countsMembersAndAverage() {
        GroupStatistics stats = GroupStatistics.ofAll(AREAS, List.of(
                List.of(student(1, 4.0), student(2, 2.0), student(3, 3.0), student(4, 3.0), student(5, 3.0))))
                .get(0);

        assertEquals(1, stats.number());
        assertEquals(5, stats.memberCount());
        assertEquals(3.0, stats.average(), EPS);
        assertEquals(0.0, stats.gapToOthers(), EPS);
        assertTrue(stats.balanced());
    }

    @Test
    @DisplayName("Média, mínimo, máximo, fortes e distribuição por competência")
    void areaStatistics() {
        GroupStatistics stats = GroupStatistics.ofAll(AREAS, List.of(List.of(
                withLevels(1, 5.0, 2.0, 1.2, 3.0),
                withLevels(2, 3.5, 2.0, 1.4, 3.0),
                withLevels(3, 1.0, 2.0, 2.6, 3.0),
                withLevels(4, 2.4, 2.0, null, 3.0)))).get(0);

        AreaStatistics org = stats.areas().get(0);
        assertEquals("ORG", org.competencyCode());
        assertEquals((5.0 + 3.5 + 1.0 + 2.4) / 4, org.average(), EPS);
        assertEquals(1.0, org.min(), EPS);
        assertEquals(5.0, org.max(), EPS);
        assertEquals(4, org.membersWithData());
        assertEquals(2, org.strongMembers());
        // 1,0 → 1; 2,4 → 2; 3,5 → 4; 5,0 → 5
        assertEquals(Map.of(1, 1, 2, 1, 3, 0, 4, 1, 5, 1), org.distribution());

        AreaStatistics prog = stats.areas().get(2);
        assertEquals(3, prog.membersWithData());
        assertEquals((1.2 + 1.4 + 2.6) / 3, prog.average(), EPS);
        assertEquals(Map.of(1, 2, 2, 0, 3, 1, 4, 0, 5, 0), prog.distribution());
    }

    @Test
    @DisplayName("Lacunas: áreas sem ninguém forte (nível ≥ 3,5)")
    void gapsAreAreasWithoutAStrongMember() {
        GroupStatistics stats = GroupStatistics.ofAll(AREAS, List.of(List.of(
                withLevels(1, 4.0, 3.4, 1.0, 3.5),
                withLevels(2, 2.0, 3.0, 2.0, 1.0),
                withLevels(3, 2.0, 3.0, 2.0, 1.0),
                withLevels(4, 2.0, 3.0, 2.0, 1.0)))).get(0);

        assertEquals(List.of("TEMPO", "PROG"), stats.gaps());
        assertEquals(List.of(), stats.areasWithoutData());
    }

    @Test
    @DisplayName("Área sem dados: média, mínimo e máximo vazios, conta como lacuna")
    void anAreaWithoutDataIsEmpty() {
        GroupStatistics stats = GroupStatistics.ofAll(AREAS, List.of(List.of(
                withLevels(1, 4.0, 4.0, 4.0, null),
                withLevels(2, 3.0, 3.0, 3.0, null),
                withLevels(3, 3.0, 3.0, 3.0, null),
                withLevels(4, 3.0, 3.0, 3.0, null)))).get(0);

        AreaStatistics equipa = stats.areas().get(3);
        assertNull(equipa.average());
        assertNull(equipa.min());
        assertNull(equipa.max());
        assertEquals(0, equipa.membersWithData());
        assertTrue(equipa.hasNoData());
        assertEquals(List.of("EQUIPA"), stats.areasWithoutData());
        assertTrue(stats.gaps().contains("EQUIPA"));
    }

    @Test
    @DisplayName("Diferença para os outros grupos e equilíbrio (limite 0,5)")
    void gapToOthersAndBalance() {
        List<GroupStatistics> stats = GroupStatistics.ofAll(AREAS, List.of(
                List.of(student(1, 4.0), student(2, 4.0), student(3, 4.0), student(4, 4.0)),
                List.of(student(5, 3.0), student(6, 3.0), student(7, 3.0), student(8, 3.0)),
                List.of(student(9, 3.5), student(10, 3.5), student(11, 3.5), student(12, 3.5))));

        // grupo 1: 4,0 − (3,0 + 3,5) / 2 = 0,75 → desequilibrado
        assertEquals(0.75, stats.get(0).gapToOthers(), EPS);
        assertFalse(stats.get(0).balanced());
        // grupo 2: 3,0 − 3,75 = −0,75 → desequilibrado
        assertEquals(-0.75, stats.get(1).gapToOthers(), EPS);
        assertFalse(stats.get(1).balanced());
        // grupo 3: 3,5 − 3,5 = 0 → equilibrado
        assertEquals(0.0, stats.get(2).gapToOthers(), EPS);
        assertTrue(stats.get(2).balanced());
        assertEquals(List.of(1, 2, 3), stats.stream().map(GroupStatistics::number).toList());
    }

    @Test
    @DisplayName("Diferença de exatamente 0,5 ainda conta como equilibrado")
    void aGapOfExactlyHalfIsBalanced() {
        List<GroupStatistics> stats = GroupStatistics.ofAll(AREAS, List.of(
                List.of(student(1, 3.5), student(2, 3.5), student(3, 3.5), student(4, 3.5)),
                List.of(student(5, 3.0), student(6, 3.0), student(7, 3.0), student(8, 3.0))));

        assertEquals(0.5, stats.get(0).gapToOthers(), EPS);
        assertTrue(stats.get(0).balanced());
    }

    @Test
    @DisplayName("Aluno sem dados conta 3,0 na média do grupo, como no algoritmo")
    void aStudentWithoutDataCountsAsNeutral() {
        GroupStatistics stats = GroupStatistics.ofAll(AREAS, List.of(List.of(
                student(1, 5.0), student(2, 5.0), student(3, 5.0),
                withLevels(4, null, null, null, null)))).get(0);

        assertEquals((5.0 + 5.0 + 5.0 + 3.0) / 4, stats.average(), EPS);
        assertEquals(3, stats.areas().get(0).membersWithData());
    }

    @Test
    @DisplayName("Sem competências, ou com um grupo vazio ou em falta, dá erro")
    void invalidInputIsRejected() {
        List<List<GroupCandidate>> oneGroup = List.of(List.of(student(1, 3.0)));

        assertThrows(IllegalArgumentException.class, () -> GroupStatistics.ofAll(List.of(), oneGroup));
        assertThrows(IllegalArgumentException.class, () -> GroupStatistics.ofAll(null, oneGroup));
        assertThrows(IllegalArgumentException.class,
                () -> GroupStatistics.ofAll(AREAS, List.of(List.of(student(1, 3.0)), List.of())));
        assertThrows(IllegalArgumentException.class,
                () -> GroupStatistics.ofAll(AREAS, Arrays.asList(List.of(student(1, 3.0)), null)));
        assertThrows(NullPointerException.class, () -> GroupStatistics.ofAll(AREAS, null));
    }

    @Test
    @DisplayName("Sem grupos devolve lista vazia")
    void noGroupsGivesAnEmptyList() {
        assertTrue(GroupStatistics.ofAll(AREAS, List.of()).isEmpty());
    }
}

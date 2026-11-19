package com.studymatch.grouping;

import static com.studymatch.grouping.SnakeGroupFormationTest.createStudents;
import static com.studymatch.grouping.SnakeGroupFormationTest.levels;
import static com.studymatch.grouping.SnakeGroupFormationTest.student;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Testes do resultado e da explicação dos grupos (Issue #71, ADR-003 D6).
 */
@DisplayName("Explicação dos grupos formados")
class GroupExplanationTest {

    private static final double EPS = 1e-9;
    private static final List<String> AREAS = List.of("ORG", "TEMPO", "PROG", "EQUIPA");

    private final SnakeGroupFormation formation = new SnakeGroupFormation();

    private static List<GroupCandidate> group(GroupCandidate... members) {
        return new ArrayList<>(List.of(members));
    }

    @Test
    @DisplayName("form devolve os grupos numerados a partir de 1, com todos os alunos")
    void formNumbersTheGroups() {
        GroupFormationResult result = formation.form(AREAS, createStudents(13));

        assertEquals(3, result.groups().size());
        assertEquals(1, result.groups().get(0).number());
        assertEquals(3, result.groups().get(2).number());
        assertEquals(13, result.studentCount());
    }

    @Test
    @DisplayName("Grupos equilibrados: diferença 0 para os outros e maxAverageGap 0")
    void balancedGroupsHaveNoGap() {
        GroupFormationResult result = formation.explain(AREAS, List.of(
                group(student(1, 4.0), student(2, 2.0), student(3, 3.0), student(4, 3.0)),
                group(student(5, 3.5), student(6, 2.5), student(7, 3.0), student(8, 3.0))));

        assertEquals(3.0, result.groups().get(0).average(), EPS);
        assertEquals(0.0, result.groups().get(0).gapToOthers(), EPS);
        assertEquals(0.0, result.maxAverageGap(), EPS);
    }

    @Test
    @DisplayName("Diferença para os outros = média do grupo − média dos outros grupos")
    void gapToOthers() {
        GroupFormationResult result = formation.explain(AREAS, List.of(
                group(student(1, 4.0), student(2, 4.0), student(3, 4.0), student(4, 4.0)),
                group(student(5, 3.0), student(6, 3.0), student(7, 3.0), student(8, 3.0)),
                group(student(9, 2.0), student(10, 2.0), student(11, 2.0), student(12, 2.0))));

        assertEquals(4.0 - 2.5, result.groups().get(0).gapToOthers(), EPS);
        assertEquals(0.0, result.groups().get(1).gapToOthers(), EPS);
        assertEquals(2.0 - 3.5, result.groups().get(2).gapToOthers(), EPS);
        assertEquals(2.0, result.maxAverageGap(), EPS);
    }

    @Test
    @DisplayName("Um só grupo: diferença 0")
    void singleGroupHasNoGap() {
        GroupFormationResult result = formation.form(AREAS, createStudents(5));

        assertEquals(0.0, result.groups().get(0).gapToOthers(), EPS);
        assertEquals(0.0, result.maxAverageGap(), EPS);
    }

    @Test
    @DisplayName("Mostra o membro mais forte em cada área")
    void strongestByArea() {
        GroupCandidate ana = new GroupCandidate(1L, "A001", "Ana", levels(5.0, 2.0, 1.0, 3.0));
        GroupCandidate bruno = new GroupCandidate(2L, "A002", "Bruno", levels(2.0, 4.5, 2.0, 3.0));
        GroupCandidate carla = new GroupCandidate(3L, "A003", "Carla", levels(1.0, 1.0, 4.0, 2.0));
        GroupCandidate duarte = new GroupCandidate(4L, "A004", "Duarte", levels(3.0, 3.0, 3.0, 4.8));

        Map<String, GroupCandidate> strongest = SnakeGroupFormation.strongestByArea(AREAS,
                List.of(ana, bruno, carla, duarte));

        assertEquals(ana, strongest.get("ORG"));
        assertEquals(bruno, strongest.get("TEMPO"));
        assertEquals(carla, strongest.get("PROG"));
        assertEquals(duarte, strongest.get("EQUIPA"));
    }

    @Test
    @DisplayName("Empate no mais forte: fica o número de estudante mais baixo")
    void strongestTieUsesStudentNumber() {
        GroupCandidate a2 = new GroupCandidate(2L, "A002", "B", levels(4.0));
        GroupCandidate a1 = new GroupCandidate(1L, "A001", "A", levels(4.0));

        Map<String, GroupCandidate> strongest = SnakeGroupFormation.strongestByArea(AREAS, List.of(a2, a1));

        assertEquals(a1, strongest.get("PROG"));
    }

    @Test
    @DisplayName("Uma área sem dados no grupo fica de fora da explicação")
    void areaWithoutDataIsLeftOut() {
        GroupCandidate a = new GroupCandidate(1L, "A001", "A", levels(4.0, 3.0, null, 3.0));
        GroupCandidate b = new GroupCandidate(2L, "A002", "B", levels(2.0, 3.0, null, 3.0));

        Map<String, GroupCandidate> strongest = SnakeGroupFormation.strongestByArea(AREAS, List.of(a, b));

        assertFalse(strongest.containsKey("PROG"));
        assertEquals(3, strongest.size());
    }

    @Test
    @DisplayName("É preciso indicar as competências")
    void competenciesAreRequired() {
        List<GroupCandidate> students = createStudents(8);

        assertThrows(IllegalArgumentException.class, () -> formation.form(null, students));
        assertThrows(IllegalArgumentException.class, () -> formation.form(List.of(), students));
    }

    @Test
    @DisplayName("Configuração impossível chega a quem chama form")
    void impossibleConfigurationReachesCaller() {
        List<GroupCandidate> students = createStudents(11);

        assertThrows(ImpossibleGroupConfigurationException.class, () -> formation.form(AREAS, students));
    }
}

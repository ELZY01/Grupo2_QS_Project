package com.studymatch.grouping;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Testes da formação de grupos em serpentina (Issue #71).
 * Começam pelos testes do {@code profile.GroupFormationTest} (Elisio), adaptados ao {@link GroupCandidate}.
 */
@DisplayName("Testes da formação de grupos em serpentina")
class SnakeGroupFormationTest {

    private final SnakeGroupFormation groupFormation = new SnakeGroupFormation();

    @Test
    @DisplayName("Devolve uma lista vazia quando não há estudantes")
    void shouldReturnEmptyListWhenThereAreNoStudents() {
        List<List<GroupCandidate>> groups = groupFormation.distribute(List.of());

        assertTrue(groups.isEmpty());
    }

    @Test
    @DisplayName("Rejeita uma lista nula")
    void shouldRejectNullStudentList() {
        assertThrows(IllegalArgumentException.class, () -> groupFormation.distribute(null));
    }

    @Test
    @DisplayName("Rejeita estudantes nulos")
    void shouldRejectNullStudents() {
        List<GroupCandidate> students = new ArrayList<>();
        students.add(null);

        assertThrows(IllegalArgumentException.class, () -> groupFormation.distribute(students));
    }

    @Test
    @DisplayName("Um estudante sem perfil não chega a ser candidato")
    void shouldRejectStudentsWithoutProfile() {
        assertThrows(NullPointerException.class,
                () -> GroupCandidate.fromProfile(1L, "A001", "Estudante 1", null));
    }

    @Test
    @DisplayName("Um estudante sem número não chega a ser candidato")
    void shouldRejectStudentsWithoutStudentNumber() {
        assertThrows(NullPointerException.class,
                () -> new GroupCandidate(1L, null, "Estudante 1", levels(3.0)));
    }

    @Test
    @DisplayName("Forma dois grupos de quatro estudantes")
    void shouldCreateTwoGroupsOfFourStudents() {
        List<List<GroupCandidate>> groups = groupFormation.distribute(createStudents(8));

        assertEquals(2, groups.size());
        assertEquals(4, groups.get(0).size());
        assertEquals(4, groups.get(1).size());
    }

    @Test
    @DisplayName("Forma dois grupos de cinco estudantes")
    void shouldCreateTwoGroupsOfFiveStudents() {
        List<List<GroupCandidate>> groups = groupFormation.distribute(createStudents(10));

        assertEquals(2, groups.size());
        assertEquals(5, groups.get(0).size());
        assertEquals(5, groups.get(1).size());
    }

    @Test
    @DisplayName("Rejeita uma quantidade impossível de estudantes")
    void shouldRejectImpossibleNumberOfStudents() {
        List<GroupCandidate> students = createStudents(6);

        assertThrows(IllegalArgumentException.class, () -> groupFormation.distribute(students));
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 5, 8, 9, 10, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21})
    @DisplayName("Todos os grupos têm entre quatro e cinco estudantes")
    void shouldCreateGroupsWithValidSizes(int numberOfStudents) {
        List<List<GroupCandidate>> groups = groupFormation.distribute(createStudents(numberOfStudents));

        assertFalse(groups.isEmpty());
        for (List<GroupCandidate> group : groups) {
            assertTrue(group.size() >= SnakeGroupFormation.MIN_GROUP_SIZE,
                    "Um grupo não pode ter menos de quatro estudantes.");
            assertTrue(group.size() <= SnakeGroupFormation.MAX_GROUP_SIZE,
                    "Um grupo não pode ter mais de cinco estudantes.");
        }
        assertEquals(numberOfStudents, groups.stream().mapToInt(List::size).sum());
    }

    @Test
    @DisplayName("Cada estudante é distribuído uma única vez")
    void shouldDistributeEveryStudentExactlyOnce() {
        List<List<GroupCandidate>> groups = groupFormation.distribute(createStudents(13));

        assertEquals(13, groups.stream().mapToInt(List::size).sum());
        long uniqueStudents = groups.stream()
                .flatMap(List::stream)
                .map(GroupCandidate::studentId)
                .distinct()
                .count();
        assertEquals(13, uniqueStudents);
    }

    @Test
    @DisplayName("A distribuição é determinística")
    void shouldDistributeStudentsDeterministically() {
        List<GroupCandidate> students = createStudents(8);

        assertEquals(groupFormation.distribute(students), groupFormation.distribute(students));
    }

    @Test
    @DisplayName("Aceita estudantes sem dados de competências")
    void shouldAcceptStudentsWithoutCompetencyData() {
        List<GroupCandidate> students = createStudents(8);
        Map<String, Double> noData = new LinkedHashMap<>();
        noData.put("ORG", null);
        students.set(0, new GroupCandidate(1L, "A001", "Estudante 1", noData));

        List<List<GroupCandidate>> groups = groupFormation.distribute(students);

        assertEquals(2, groups.size());
        assertEquals(8, groups.stream().mapToInt(List::size).sum());
    }

    @Test
    @DisplayName("O aluno com a maior média vai para o grupo 1 e a serpentina equilibra as médias")
    void strongestStudentGoesToFirstGroup() {
        // médias 5,0; 4,5; 4,0; ...; 1,5 (alunos 1 a 8, por ordem de entrada invertida)
        List<GroupCandidate> students = new ArrayList<>();
        for (int i = 8; i >= 1; i--) {
            students.add(student(i, 5.0 - (i - 1) * 0.5));
        }

        List<List<GroupCandidate>> groups = groupFormation.distribute(students);

        // serpentina: G1, G2, G2, G1, G1, G2, G2, G1
        assertEquals(List.of(1L, 4L, 5L, 8L), ids(groups.get(0)));
        assertEquals(List.of(2L, 3L, 6L, 7L), ids(groups.get(1)));
        assertEquals(3.25, averageOf(groups.get(0)), EPS);
        assertEquals(3.25, averageOf(groups.get(1)), EPS);
    }

    @Test
    @DisplayName("Com médias iguais, desempata pelo número de estudante")
    void tieIsBrokenByStudentNumber() {
        List<GroupCandidate> students = new ArrayList<>(createStudents(8));
        java.util.Collections.reverse(students);

        List<List<GroupCandidate>> groups = groupFormation.distribute(students);

        assertEquals(List.of(1L, 4L, 5L, 8L), ids(groups.get(0)));
        assertEquals(List.of(2L, 3L, 6L, 7L), ids(groups.get(1)));
    }

    @Test
    @DisplayName("Um aluno sem dados entra na ordenação com a média neutra 3,0, não no fim")
    void studentWithoutDataUsesNeutralAverage() {
        // 9 alunos -> grupos de 5 e 4; posições G1 G2 G2 G1 G1 G2 G2 G1 G1
        double[] averages = {5.0, 4.5, 4.0, 3.5, 3.2, 2.0, 1.5, 1.0};
        List<GroupCandidate> students = new ArrayList<>();
        for (int i = 0; i < averages.length; i++) {
            students.add(student(i + 1, averages[i]));
        }
        Map<String, Double> noData = levels(null, null, null, null);
        GroupCandidate withoutData = new GroupCandidate(99L, "A099", "Sem dados", noData);
        students.add(withoutData);

        List<List<GroupCandidate>> groups = groupFormation.distribute(students);

        // 3,0 fica entre 3,2 e 2,0: 6.ª posição -> grupo 2 (no fim iria para o grupo 1)
        assertTrue(groups.get(1).contains(withoutData), "O aluno sem dados devia estar no grupo 2.");
    }

    // --- dados de teste --------------------------------------------------------

    private static final double EPS = 1e-9;

    static GroupCandidate student(int id, double level) {
        return new GroupCandidate((long) id, String.format("A%03d", id), "Estudante " + id, levels(level));
    }

    static List<Long> ids(List<GroupCandidate> group) {
        return group.stream().map(GroupCandidate::studentId).toList();
    }

    static double averageOf(List<GroupCandidate> group) {
        return group.stream().mapToDouble(GroupCandidate::sortingAverage).average().orElseThrow();
    }

    static List<GroupCandidate> createStudents(int numberOfStudents) {
        List<GroupCandidate> students = new ArrayList<>();
        for (int i = 1; i <= numberOfStudents; i++) {
            students.add(new GroupCandidate((long) i, String.format("A%03d", i), "Estudante " + i, levels(3.0)));
        }
        return students;
    }

    static Map<String, Double> levels(double level) {
        return levels(level, level, level, level);
    }

    static Map<String, Double> levels(Double org, Double tempo, Double prog, Double equipa) {
        Map<String, Double> levels = new LinkedHashMap<>();
        levels.put("ORG", org);
        levels.put("TEMPO", tempo);
        levels.put("PROG", prog);
        levels.put("EQUIPA", equipa);
        return levels;
    }
}

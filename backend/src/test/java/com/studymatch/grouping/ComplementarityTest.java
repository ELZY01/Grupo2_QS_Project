package com.studymatch.grouping;

import static com.studymatch.grouping.SnakeGroupFormationTest.ids;
import static com.studymatch.grouping.SnakeGroupFormationTest.levels;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Testes da complementaridade: trocas que dão a cada grupo alguém forte em cada área,
 * sem passar o limite de equilíbrio (Issue #71, ADR-003 D4).
 */
@DisplayName("Complementaridade entre grupos")
class ComplementarityTest {

    private static final double EPS = 1e-9;
    private static final List<String> AREAS = List.of("ORG", "TEMPO", "PROG", "EQUIPA");

    private final SnakeGroupFormation formation = new SnakeGroupFormation();

    private static GroupCandidate s(int id, double org, double tempo, double prog, double equipa) {
        return new GroupCandidate((long) id, String.format("A%03d", id), "Estudante " + id,
                levels(org, tempo, prog, equipa));
    }

    /** Depois da serpentina, o grupo 2 fica sem ninguém forte em Programação. */
    private static List<GroupCandidate> groupTwoWithoutProgramming() {
        return List.of(
                s(1, 4.0, 4.0, 4.0, 4.0),   // média 4,00 -> G1, forte em tudo
                s(2, 4.0, 4.0, 1.0, 4.0),   // 3,25 -> G2, sem Programação
                s(3, 3.2, 3.2, 3.2, 3.2),   // 3,20 -> G2
                s(4, 2.0, 2.0, 4.0, 4.0),   // 3,00 -> G1, forte em Programação
                s(5, 2.0, 2.0, 4.0, 3.8),   // 2,95 -> G1, forte em Programação
                s(6, 2.9, 2.9, 2.9, 2.9),   // 2,90 -> G2
                s(7, 2.8, 2.8, 2.8, 2.8),   // 2,80 -> G2
                s(8, 2.7, 2.7, 2.7, 2.7));  // 2,70 -> G1
    }

    @Test
    @DisplayName("Só a serpentina deixa um grupo sem Programação")
    void snakeAloneLeavesAGap() {
        List<List<GroupCandidate>> groups = formation.distribute(groupTwoWithoutProgramming());

        assertEquals(List.of(1L, 4L, 5L, 8L), ids(groups.get(0)));
        assertEquals(4, SnakeGroupFormation.coverage(AREAS, groups.get(0)));
        assertEquals(3, SnakeGroupFormation.coverage(AREAS, groups.get(1)));
    }

    @Test
    @DisplayName("Depois das trocas, todos os grupos têm alguém forte nas 4 áreas")
    void swapsCoverEveryArea() {
        GroupFormationResult result = formation.form(AREAS, groupTwoWithoutProgramming());

        for (FormedGroup group : result.groups()) {
            assertEquals(4, SnakeGroupFormation.coverage(AREAS, group.members()),
                    "O grupo " + group.number() + " devia cobrir as 4 áreas.");
        }
        assertTrue(result.maxAverageGap() <= GroupFormation.MAX_AVERAGE_GAP + EPS);
    }

    @Test
    @DisplayName("As trocas mantêm os tamanhos e cada aluno num só grupo")
    void swapsKeepSizesAndMembers() {
        GroupFormationResult result = formation.form(AREAS, groupTwoWithoutProgramming());

        assertEquals(List.of(4, 4), result.groups().stream().map(FormedGroup::size).toList());
        List<Long> all = new ArrayList<>();
        result.groups().forEach(g -> all.addAll(ids(g.members())));
        assertEquals(8, all.size());
        assertEquals(8L, all.stream().distinct().count());
    }

    @Test
    @DisplayName("Não troca se todas as trocas úteis passassem o limite de 0,5")
    void noSwapWhenBalanceWouldBreak() {
        // Encontrado por pesquisa: qualquer troca que cobre mais áreas deixa a diferença entre 0,53 e 0,97.
        List<GroupCandidate> students = List.of(
                s(1, 4.5, 5.0, 2.5, 5.0), s(2, 3.5, 1.5, 1.5, 5.0), s(3, 1.0, 3.5, 4.0, 2.0),
                s(4, 4.0, 1.0, 1.0, 2.0), s(5, 1.5, 1.5, 2.0, 5.0), s(6, 2.0, 2.5, 1.0, 3.0),
                s(7, 4.5, 2.5, 5.0, 2.5), s(8, 1.0, 1.5, 2.5, 4.5));

        List<List<GroupCandidate>> snake = formation.distribute(students);
        GroupFormationResult result = formation.form(AREAS, students);

        assertEquals(ids(snake.get(0)), ids(result.groups().get(0).members()));
        assertEquals(ids(snake.get(1)), ids(result.groups().get(1).members()));
        assertTrue(result.maxAverageGap() <= GroupFormation.MAX_AVERAGE_GAP + EPS);
    }

    @Test
    @DisplayName("Se já estão todos cobertos, não há trocas")
    void nothingToImprove() {
        List<GroupCandidate> students = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            students.add(s(i, 4.0, 4.0, 4.0, 4.0));
        }

        List<List<GroupCandidate>> snake = formation.distribute(students);

        assertEquals(snake, formation.improveComplementarity(AREAS, snake));
    }

    @Test
    @DisplayName("O resultado é sempre o mesmo para os mesmos dados")
    void deterministic() {
        GroupFormationResult first = formation.form(AREAS, groupTwoWithoutProgramming());
        GroupFormationResult second = formation.form(AREAS, groupTwoWithoutProgramming());

        assertEquals(first, second);
    }

    @Test
    @DisplayName("Não altera a lista de grupos recebida")
    void doesNotChangeInput() {
        List<List<GroupCandidate>> snake = formation.distribute(groupTwoWithoutProgramming());
        List<Long> before = ids(snake.get(0));

        formation.improveComplementarity(AREAS, snake);

        assertEquals(before, ids(snake.get(0)));
    }
}

package com.studymatch.grouping;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Formação de grupos em serpentina (Issue #71, ADR-003).
 *
 * <p>Ponto de partida: a distribuição feita pelo Elisio em {@code profile.GroupFormation},
 * trazida para o pacote {@code grouping} e adaptada ao {@link GroupCandidate}.
 * Neste passo o comportamento é o mesmo; as regras do ADR-003 entram nos passos seguintes.
 */
public class SnakeGroupFormation {

    public static final int MIN_GROUP_SIZE = 4;
    public static final int MAX_GROUP_SIZE = 5;

    /**
     * Distribui os alunos por grupos de 4 ou 5, ordenando-os pela média do perfil
     * e aplicando uma distribuição em serpentina.
     */
    public List<List<GroupCandidate>> distribute(List<GroupCandidate> students) {

        if (students == null) {
            throw new IllegalArgumentException("A lista de estudantes não pode ser nula.");
        }

        if (students.isEmpty()) {
            return List.of();
        }

        validateStudents(students);

        int numberOfGroups = (students.size() + MAX_GROUP_SIZE - 1) / MAX_GROUP_SIZE;
        int minimumStudents = numberOfGroups * MIN_GROUP_SIZE;

        if (students.size() < minimumStudents) {
            throw new IllegalArgumentException("Não é possível formar grupos de 4 ou 5 estudantes.");
        }

        List<GroupCandidate> ordered = new ArrayList<>(students);
        ordered.sort(Comparator
                .comparing(SnakeGroupFormation::averageOrNull, Comparator.nullsLast(Double::compareTo))
                .thenComparing(GroupCandidate::studentNumber));

        List<List<GroupCandidate>> groups = new ArrayList<>();
        for (int i = 0; i < numberOfGroups; i++) {
            groups.add(new ArrayList<>());
        }

        int groupIndex = 0;
        int direction = 1;
        for (GroupCandidate student : ordered) {
            groups.get(groupIndex).add(student);

            if (direction == 1 && groupIndex == numberOfGroups - 1) {
                direction = -1;
            } else if (direction == -1 && groupIndex == 0) {
                direction = 1;
            } else {
                groupIndex += direction;
            }
        }

        return groups;
    }

    /** O número e o perfil já são validados pelo próprio {@link GroupCandidate}. */
    private void validateStudents(List<GroupCandidate> students) {
        for (GroupCandidate student : students) {
            if (student == null) {
                throw new IllegalArgumentException("A lista não pode conter estudantes nulos.");
            }
        }
    }

    /** Média das competências com dados, ou {@code null} se o aluno não tem dados nenhuns. */
    private static Double averageOrNull(GroupCandidate student) {
        return student.average().isPresent() ? student.average().getAsDouble() : null;
    }
}

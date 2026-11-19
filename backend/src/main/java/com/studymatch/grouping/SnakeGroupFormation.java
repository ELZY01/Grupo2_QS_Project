package com.studymatch.grouping;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Formação de grupos em serpentina (Issue #71, ADR-003).
 *
 * <p>Ponto de partida: a distribuição feita pelo Elisio em {@code profile.GroupFormation},
 * trazida para o pacote {@code grouping} e adaptada ao {@link GroupCandidate}.
 *
 * <p>Ordenação (ADR-003 D3 e D5): média do perfil da maior para a menor; empate pelo
 * número de estudante; um aluno sem dados nenhuns entra com a média neutra 3,0.
 */
public class SnakeGroupFormation {

    public static final int MIN_GROUP_SIZE = 4;
    public static final int MAX_GROUP_SIZE = 5;

    /** Maior média primeiro; empate pelo número de estudante (único, logo determinístico). */
    static final Comparator<GroupCandidate> BY_AVERAGE_DESC = Comparator
            .comparingDouble(GroupCandidate::sortingAverage).reversed()
            .thenComparing(GroupCandidate::studentNumber);

    /**
     * Distribui os alunos por grupos de 4 ou 5, ordenando-os pela média do perfil
     * (da maior para a menor) e aplicando uma distribuição em serpentina.
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
        ordered.sort(BY_AVERAGE_DESC);

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

}

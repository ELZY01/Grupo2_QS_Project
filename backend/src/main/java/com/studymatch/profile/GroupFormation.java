
package com.studymatch.profile;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Responsavel pela formacao automatica de grupos equilibrados.
 */
public class GroupFormation {

    public static final int MIN_GROUP_SIZE = 4;
    public static final int MAX_GROUP_SIZE = 5;
    public static final double MAX_AVERAGE_GAP = 0.5;

    /**
     * Distribui estudantes por grupos de 4 ou 5,
     * ordenando os perfis e aplicando uma distribuicao
     * em serpentina.
     */
    public List<List<StudentProfileEntry>> distribute(
            List<StudentProfileEntry> students) {

        if (students == null) {
            throw new IllegalArgumentException(
                    "A lista de estudantes nao pode ser nula.");
        }

        if (students.isEmpty()) {
            return List.of();
        }

        validateStudents(students);

        int numberOfGroups = (students.size() + MAX_GROUP_SIZE - 1)
                / MAX_GROUP_SIZE;

        int minimumStudents = numberOfGroups * MIN_GROUP_SIZE;

        if (students.size() < minimumStudents) {
            throw new IllegalArgumentException(
                    "Nao e possivel formar grupos de 4 ou 5 estudantes.");
        }

        List<StudentProfileEntry> ordered = new ArrayList<>(students);

        ordered.sort(
                Comparator.comparing(
                        this::calculateAverage,
                        Comparator.nullsLast(Double::compareTo)
                ).thenComparing(StudentProfileEntry::studentNumber)
        );

        List<List<StudentProfileEntry>> groups = new ArrayList<>();

        for (int i = 0; i < numberOfGroups; i++) {
            groups.add(new ArrayList<>());
        }

        int groupIndex = 0;
        int direction = 1;

        for (StudentProfileEntry student : ordered) {

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

    /**
     * Valida os dados dos estudantes antes da distribuicao.
     */
    private void validateStudents(List<StudentProfileEntry> students) {

        for (StudentProfileEntry student : students) {

            if (student == null) {
                throw new IllegalArgumentException(
                        "A lista nao pode conter estudantes nulos.");
            }

            if (student.profile() == null) {
                throw new IllegalArgumentException(
                        "O perfil do estudante nao pode ser nulo.");
            }

            if (student.studentNumber() == null
                    || student.studentNumber().isBlank()) {
                throw new IllegalArgumentException(
                        "O numero de estudante e obrigatorio.");
            }
        }
    }

    /**
     * Calcula a media das competencias com dados disponiveis.
     * Competencias sem dados nao entram no calculo.
     * Se nao existir qualquer competencia com dados,
     * devolve null em vez de atribuir uma media de zero.
     */
    private Double calculateAverage(StudentProfileEntry student) {

        List<CompetencyLevel> competencies =
                student.profile().competencies();

        double total = 0.0;
        int count = 0;

        for (CompetencyLevel competency : competencies) {

            if (competency.hasData() && competency.level() != null) {
                total += competency.level();
                count++;
            }
        }

        if (count == 0) {
            return null;
        }

        return total / count;
    }
}

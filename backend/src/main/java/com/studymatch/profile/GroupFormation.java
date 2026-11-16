
package com.studymatch.profile;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Responsavel pela formacao automatica de grupos equilibrados.
 */
public class GroupFormation {

    public static final int MIN_GROUP_SIZE = 4;
    public static final int MAX_GROUP_SIZE = 5;
    public static final double MAX_AVERAGE_GAP = 0.5;

    private static final List<String> COMPETENCIES =
            List.of("ORG", "TEMPO", "PROG", "EQUIPA");

    /**
     * Distribui estudantes por grupos de 4 ou 5,
     * ordena os perfis e aplica a distribuicao em serpentina.
     * Depois tenta melhorar a complementaridade dos grupos.
     */
    public List<List<StudentProfileEntry>> distribute(
            List<StudentProfileEntry> students) {

        if (students == null) {
            throw new IllegalArgumentException(
                    "A lista de estudantes nao pode ser nula.");
        }

        if (students.isEmpty()) {
            throw new IllegalArgumentException(
                    "A lista de estudantes nao pode estar vazia.");
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

        distributeInSnake(ordered, groups);

        improveComplementarity(groups);

        return groups;
    }

    /**
     * Distribui os estudantes em serpentina.
     */
    private void distributeInSnake(
            List<StudentProfileEntry> students,
            List<List<StudentProfileEntry>> groups) {

        int groupIndex = 0;
        int direction = 1;
        int numberOfGroups = groups.size();

        for (StudentProfileEntry student : students) {

            groups.get(groupIndex).add(student);

            if (direction == 1 && groupIndex == numberOfGroups - 1) {
                direction = -1;
            } else if (direction == -1 && groupIndex == 0) {
                direction = 1;
            } else {
                groupIndex += direction;
            }
        }
    }

    /**
     * Tenta melhorar a complementaridade atraves de trocas.
     * Uma troca so e aceite se melhorar a complementaridade
     * e mantiver a diferenca das medias dentro do limite.
     */
    private void improveComplementarity(
            List<List<StudentProfileEntry>> groups) {

        int currentScore = calculateComplementarityScore(groups);

        boolean improved = true;

        while (improved) {

            improved = false;

            int bestScore = currentScore;
            int bestGroupA = -1;
            int bestGroupB = -1;
            int bestStudentA = -1;
            int bestStudentB = -1;

            for (int groupA = 0; groupA < groups.size(); groupA++) {

                for (int groupB = groupA + 1;
                     groupB < groups.size(); groupB++) {

                    List<StudentProfileEntry> firstGroup =
                            groups.get(groupA);

                    List<StudentProfileEntry> secondGroup =
                            groups.get(groupB);

                    for (int studentA = 0;
                         studentA < firstGroup.size(); studentA++) {

                        for (int studentB = 0;
                             studentB < secondGroup.size(); studentB++) {

                            swap(
                                    firstGroup,
                                    studentA,
                                    secondGroup,
                                    studentB
                            );

                            double averageGap =
                                    calculateAverageGap(groups);

                            int newScore =
                                    calculateComplementarityScore(groups);

                            swap(
                                    firstGroup,
                                    studentA,
                                    secondGroup,
                                    studentB
                            );

                            if (averageGap <= MAX_AVERAGE_GAP
                                    && newScore > bestScore) {

                                bestScore = newScore;
                                bestGroupA = groupA;
                                bestGroupB = groupB;
                                bestStudentA = studentA;
                                bestStudentB = studentB;
                            }
                        }
                    }
                }
            }

            if (bestGroupA != -1) {

                swap(
                        groups.get(bestGroupA),
                        bestStudentA,
                        groups.get(bestGroupB),
                        bestStudentB
                );

                currentScore = bestScore;
                improved = true;
            }
        }
    }

    /**
     * Calcula a complementaridade.
     * Para cada competencia, considera os k estudantes
     * com os niveis mais elevados, sendo k o numero de grupos.
     * Cada grupo recebe um ponto se tiver pelo menos um deles.
     */
    private int calculateComplementarityScore(
            List<List<StudentProfileEntry>> groups) {

        int score = 0;

        for (String competencyCode : COMPETENCIES) {

            Set<Long> strongestStudents =
                    findStrongestStudents(groups, competencyCode);

            for (List<StudentProfileEntry> group : groups) {

                boolean hasStrongStudent = false;

                for (StudentProfileEntry student : group) {

                    if (strongestStudents.contains(student.studentId())) {
                        hasStrongStudent = true;
                        break;
                    }
                }

                if (hasStrongStudent) {
                    score++;
                }
            }
        }

        return score;
    }

    /**
     * Identifica os k estudantes mais fortes numa competencia.
     */
    private Set<Long> findStrongestStudents(
            List<List<StudentProfileEntry>> groups,
            String competencyCode) {

        List<StudentProfileEntry> candidates = new ArrayList<>();

        for (List<StudentProfileEntry> group : groups) {
            for (StudentProfileEntry student : group) {

                if (getCompetencyLevel(student, competencyCode) != null) {
                    candidates.add(student);
                }
            }
        }

        candidates.sort(
                Comparator.comparing(
                        (StudentProfileEntry student) ->
                                getCompetencyLevel(student, competencyCode)
                ).reversed().thenComparing(
                        StudentProfileEntry::studentNumber
                )
        );

        Set<Long> strongestStudents = new HashSet<>();

        int limit = Math.min(groups.size(), candidates.size());

        for (int i = 0; i < limit; i++) {
            strongestStudents.add(candidates.get(i).studentId());
        }

        return strongestStudents;
    }

    /**
     * Devolve o nivel de um estudante numa competencia.
     * Se nao existirem dados, devolve null.
     */
    private Double getCompetencyLevel(
            StudentProfileEntry student,
            String competencyCode) {

        return student.profile()
                .competency(competencyCode)
                .filter(CompetencyLevel::hasData)
                .map(CompetencyLevel::level)
                .orElse(null);
    }

    /**
     * Calcula a diferenca entre a maior e a menor media
     * dos grupos.
     */
    private double calculateAverageGap(
            List<List<StudentProfileEntry>> groups) {

        Double minimumAverage = null;
        Double maximumAverage = null;

        for (List<StudentProfileEntry> group : groups) {

            Double average = calculateGroupAverage(group);

            if (average == null) {
                continue;
            }

            if (minimumAverage == null || average < minimumAverage) {
                minimumAverage = average;
            }

            if (maximumAverage == null || average > maximumAverage) {
                maximumAverage = average;
            }
        }

        if (minimumAverage == null || maximumAverage == null) {
            return 0.0;
        }

        return maximumAverage - minimumAverage;
    }

    /**
     * Calcula a media dos estudantes de um grupo.
     * Estudantes sem dados nao entram no calculo.
     */
    private Double calculateGroupAverage(
            List<StudentProfileEntry> group) {

        double total = 0.0;
        int count = 0;

        for (StudentProfileEntry student : group) {

            Double average = calculateAverage(student);

            if (average != null) {
                total += average;
                count++;
            }
        }

        if (count == 0) {
            return null;
        }

        return total / count;
    }

    /**
     * Troca dois estudantes entre grupos.
     */
    private void swap(
            List<StudentProfileEntry> firstGroup,
            int firstIndex,
            List<StudentProfileEntry> secondGroup,
            int secondIndex) {

        StudentProfileEntry firstStudent =
                firstGroup.get(firstIndex);

        StudentProfileEntry secondStudent =
                secondGroup.get(secondIndex);

        firstGroup.set(firstIndex, secondStudent);
        secondGroup.set(secondIndex, firstStudent);
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

            if (student.studentId() == null) {
                throw new IllegalArgumentException(
                        "O identificador do estudante e obrigatorio.");
            }
        }
    }

    /**
     * Calcula a media das competencias com dados disponiveis.
     * Competencias sem dados nao entram no calculo.
     * Se nao existir qualquer competencia com dados,
     * devolve null.
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

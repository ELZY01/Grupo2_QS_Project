
package com.studymatch.group;

import com.studymatch.profile.GroupFormation;
import com.studymatch.profile.CompetencyLevel;
import com.studymatch.profile.LevelOrigin;
import com.studymatch.profile.StudentProfile;
import com.studymatch.profile.StudentProfileEntry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GroupFormationTest {

    private final GroupFormation groupFormation = new GroupFormation();

    @Test
    @DisplayName("Deve rejeitar uma lista nula")
    void shouldRejectNullList() {
        assertThrows(
                IllegalArgumentException.class,
                () -> groupFormation.distribute(null)
        );
    }

    @Test
    @DisplayName("Deve rejeitar uma lista vazia")
    void shouldRejectEmptyList() {
        assertThrows(
                IllegalArgumentException.class,
                () -> groupFormation.distribute(List.of())
        );
    }

    @Test
    @DisplayName("Deve rejeitar estudantes nulos")
    void shouldRejectNullStudent() {
        List<StudentProfileEntry> students = new ArrayList<>();
        students.add(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> groupFormation.distribute(students)
        );
    }

    @Test
    @DisplayName("Deve rejeitar estudantes sem perfil")
    void shouldRejectStudentWithoutProfile() {
        List<StudentProfileEntry> students = new ArrayList<>();

        students.add(
                new StudentProfileEntry(
                        1L,
                        "A001",
                        "Estudante 1",
                        null
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> groupFormation.distribute(students)
        );
    }

    @Test
    @DisplayName("Deve rejeitar estudantes sem numero")
    void shouldRejectStudentWithoutNumber() {
        List<StudentProfileEntry> students = new ArrayList<>();

        students.add(
                new StudentProfileEntry(
                        1L,
                        null,
                        "Estudante 1",
                        createProfile(3.0)
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> groupFormation.distribute(students)
        );
    }

    @Test
    @DisplayName("Deve rejeitar estudantes sem identificador")
    void shouldRejectStudentWithoutId() {
        List<StudentProfileEntry> students = new ArrayList<>();

        students.add(
                new StudentProfileEntry(
                        null,
                        "A001",
                        "Estudante 1",
                        createProfile(3.0)
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> groupFormation.distribute(students)
        );
    }

    @Test
    @DisplayName("Deve criar dois grupos de quatro estudantes")
    void shouldCreateTwoGroupsOfFour() {
        List<StudentProfileEntry> students = createStudents(8);

        List<List<StudentProfileEntry>> groups =
                groupFormation.distribute(students);

        assertEquals(2, groups.size());
        assertEquals(4, groups.get(0).size());
        assertEquals(4, groups.get(1).size());
    }

    @Test
    @DisplayName("Deve criar dois grupos de cinco estudantes")
    void shouldCreateTwoGroupsOfFive() {
        List<StudentProfileEntry> students = createStudents(10);

        List<List<StudentProfileEntry>> groups =
                groupFormation.distribute(students);

        assertEquals(2, groups.size());
        assertEquals(5, groups.get(0).size());
        assertEquals(5, groups.get(1).size());
    }

    @Test
    @DisplayName("Deve rejeitar uma quantidade impossivel de estudantes")
    void shouldRejectImpossibleNumberOfStudents() {
        List<StudentProfileEntry> students = createStudents(6);

        assertThrows(
                IllegalArgumentException.class,
                () -> groupFormation.distribute(students)
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {
            4, 5, 8, 9, 10, 12, 13, 14,
            15, 16, 17, 18, 19, 20, 21
    })
    @DisplayName("Deve criar grupos com tamanhos validos")
    void shouldCreateValidGroupSizes(int numberOfStudents) {

        List<StudentProfileEntry> students =
                createStudents(numberOfStudents);

        List<List<StudentProfileEntry>> groups =
                groupFormation.distribute(students);

        int totalStudents = 0;

        for (List<StudentProfileEntry> group : groups) {

            assertTrue(group.size() >= GroupFormation.MIN_GROUP_SIZE);
            assertTrue(group.size() <= GroupFormation.MAX_GROUP_SIZE);

            totalStudents += group.size();
        }

        assertEquals(numberOfStudents, totalStudents);
    }

    @Test
    @DisplayName("Deve distribuir todos os estudantes apenas uma vez")
    void shouldDistributeEveryStudentExactlyOnce() {

        List<StudentProfileEntry> students = createStudents(13);

        List<List<StudentProfileEntry>> groups =
                groupFormation.distribute(students);

        List<Long> distributedIds = new ArrayList<>();

        for (List<StudentProfileEntry> group : groups) {
            for (StudentProfileEntry student : group) {
                distributedIds.add(student.studentId());
            }
        }

        assertEquals(13, distributedIds.size());
        assertEquals(13, distributedIds.stream().distinct().count());
    }

    @Test
    @DisplayName("Deve produzir uma distribuicao deterministica")
    void shouldProduceDeterministicDistribution() {

        List<StudentProfileEntry> students = createStudents(8);

        List<List<StudentProfileEntry>> firstDistribution =
                groupFormation.distribute(students);

        List<List<StudentProfileEntry>> secondDistribution =
                groupFormation.distribute(students);

        for (int i = 0; i < firstDistribution.size(); i++) {

            List<Long> firstIds = firstDistribution.get(i)
                    .stream()
                    .map(StudentProfileEntry::studentId)
                    .toList();

            List<Long> secondIds = secondDistribution.get(i)
                    .stream()
                    .map(StudentProfileEntry::studentId)
                    .toList();

            assertEquals(firstIds, secondIds);
        }
    }

    @Test
    @DisplayName("Deve aceitar estudantes sem dados de competencias")
    void shouldAcceptStudentsWithoutCompetencyData() {

        List<StudentProfileEntry> students = createStudents(8);

        students.set(
                0,
                new StudentProfileEntry(
                        1L,
                        "A001",
                        "Estudante 1",
                        new StudentProfile(0, 0.0, List.of(
                                competency("ORG", null, LevelOrigin.NO_DATA),
                                competency("TEMPO", null, LevelOrigin.NO_DATA),
                                competency("PROG", null, LevelOrigin.NO_DATA),
                                competency("EQUIPA", null, LevelOrigin.NO_DATA)
                        ))
                )
        );

        List<List<StudentProfileEntry>> groups =
                groupFormation.distribute(students);

        assertEquals(2, groups.size());
    }

    @Test
    @DisplayName("Deve separar estudantes fortes na mesma competencia")
    void shouldSeparateStrongStudentsInSameCompetency() {

        List<StudentProfileEntry> students = new ArrayList<>();

        for (int i = 1; i <= 8; i++) {

            double orgLevel = (i == 1 || i == 4) ? 5.0 : 1.0;
            double otherLevel = (i == 1 || i == 4) ? 3.0 : 11.0 / 3.0;

            students.add(
                    new StudentProfileEntry(
                            (long) i,
                            String.format("A%03d", i),
                            "Estudante " + i,
                            createProfile(orgLevel, otherLevel)
                    )
            );
        }

        List<List<StudentProfileEntry>> groups =
                groupFormation.distribute(students);

        int firstStrongStudentGroup = findGroupIndex(groups, 1L);
        int secondStrongStudentGroup = findGroupIndex(groups, 4L);

        assertNotEquals(
                firstStrongStudentGroup,
                secondStrongStudentGroup,
                "Os estudantes mais fortes na mesma competencia devem ficar em grupos diferentes."
        );
    }

    @Test
    @DisplayName("Deve manter a diferenca entre medias dentro do limite")
    void shouldMaintainAverageGapWithinLimit() {

        List<StudentProfileEntry> students = new ArrayList<>();

        double[] organizationLevels = {
                5.0, 1.0, 1.0, 5.0,
                1.0, 5.0, 5.0, 1.0
        };

        double[] otherLevels = {
                2.0, 4.0, 4.0, 2.0,
                4.0, 2.0, 2.0, 4.0
        };

        for (int i = 0; i < 8; i++) {

            students.add(
                    new StudentProfileEntry(
                            (long) (i + 1),
                            String.format("A%03d", i + 1),
                            "Estudante " + (i + 1),
                            createProfile(
                                    organizationLevels[i],
                                    otherLevels[i]
                            )
                    )
            );
        }

        List<List<StudentProfileEntry>> groups =
                groupFormation.distribute(students);

        double firstAverage = calculateAverage(groups.get(0));
        double secondAverage = calculateAverage(groups.get(1));

        double difference = Math.abs(firstAverage - secondAverage);

        assertTrue(
                difference <= GroupFormation.MAX_AVERAGE_GAP,
                "A diferenca entre as medias nao pode ultrapassar 0,5."
        );
    }

    private int findGroupIndex(
            List<List<StudentProfileEntry>> groups,
            Long studentId) {

        for (int i = 0; i < groups.size(); i++) {

            for (StudentProfileEntry student : groups.get(i)) {

                if (student.studentId().equals(studentId)) {
                    return i;
                }
            }
        }

        return -1;
    }

    private List<StudentProfileEntry> createStudents(int numberOfStudents) {

        List<StudentProfileEntry> students = new ArrayList<>();

        for (int i = 1; i <= numberOfStudents; i++) {

            students.add(
                    new StudentProfileEntry(
                            (long) i,
                            String.format("A%03d", i),
                            "Estudante " + i,
                            createProfile(3.0)
                    )
            );
        }

        return students;
    }

    private StudentProfile createProfile(double level) {
        return createProfile(level, level);
    }

    private StudentProfile createProfile(
            double organizationLevel,
            double otherLevel) {

        return new StudentProfile(
                0,
                0.0,
                List.of(
                        competency("ORG", organizationLevel, LevelOrigin.QUIZ),
                        competency("TEMPO", otherLevel, LevelOrigin.QUIZ),
                        competency("PROG", otherLevel, LevelOrigin.QUIZ),
                        competency("EQUIPA", otherLevel, LevelOrigin.QUIZ)
                )
        );
    }

    private CompetencyLevel competency(
            String code,
            Double level,
            LevelOrigin origin) {

        return new CompetencyLevel(
                code,
                level,
                origin,
                1.0,
                level,
                null,
                List.of()
        );
    }

    private double calculateAverage(List<StudentProfileEntry> group) {

        double total = 0.0;

        for (StudentProfileEntry student : group) {

            total += student.profile().competencies().stream()
                    .filter(CompetencyLevel::hasData)
                    .mapToDouble(competency -> competency.level())
                    .average()
                    .orElse(0.0);
        }

        return total / group.size();
    }
}

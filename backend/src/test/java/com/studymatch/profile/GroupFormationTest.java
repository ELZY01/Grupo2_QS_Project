
package com.studymatch.profile;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes da formacao de grupos")
class GroupFormationTest {

    private final GroupFormation groupFormation = new GroupFormation();

    @Test
    @DisplayName("Deve devolver uma lista vazia quando nao existem estudantes")
    void shouldReturnEmptyListWhenThereAreNoStudents() {

        List<List<StudentProfileEntry>> groups =
                groupFormation.distribute(List.of());

        assertTrue(groups.isEmpty());
    }

    @Test
    @DisplayName("Deve rejeitar uma lista nula")
    void shouldRejectNullStudentList() {

        assertThrows(
                IllegalArgumentException.class,
                () -> groupFormation.distribute(null)
        );
    }

    @Test
    @DisplayName("Deve rejeitar estudantes nulos")
    void shouldRejectNullStudents() {

        List<StudentProfileEntry> students = new ArrayList<>();
        students.add(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> groupFormation.distribute(students)
        );
    }

    @Test
    @DisplayName("Deve rejeitar estudantes sem perfil")
    void shouldRejectStudentsWithoutProfile() {

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
    @DisplayName("Deve rejeitar estudantes sem numero de estudante")
    void shouldRejectStudentsWithoutStudentNumber() {

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
    @DisplayName("Deve formar dois grupos de quatro estudantes")
    void shouldCreateTwoGroupsOfFourStudents() {

        List<StudentProfileEntry> students = createStudents(8);

        List<List<StudentProfileEntry>> groups =
                groupFormation.distribute(students);

        assertEquals(2, groups.size());
        assertEquals(4, groups.get(0).size());
        assertEquals(4, groups.get(1).size());
    }

    @Test
    @DisplayName("Deve formar dois grupos de cinco estudantes")
    void shouldCreateTwoGroupsOfFiveStudents() {

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

    @Test
    @DisplayName("Todos os estudantes devem ser distribuidos uma unica vez")
    void shouldDistributeEveryStudentExactlyOnce() {

        List<StudentProfileEntry> students = createStudents(13);

        List<List<StudentProfileEntry>> groups =
                groupFormation.distribute(students);

        int totalStudents = 0;

        for (List<StudentProfileEntry> group : groups) {
            totalStudents += group.size();
        }

        assertEquals(13, totalStudents);

        long uniqueStudents = groups.stream()
                .flatMap(List::stream)
                .map(StudentProfileEntry::studentId)
                .distinct()
                .count();

        assertEquals(13, uniqueStudents);
    }

    @Test
    @DisplayName("Deve distribuir os estudantes de forma deterministica")
    void shouldDistributeStudentsDeterministically() {

        List<StudentProfileEntry> students = createStudents(8);

        List<List<StudentProfileEntry>> firstResult =
                groupFormation.distribute(students);

        List<List<StudentProfileEntry>> secondResult =
                groupFormation.distribute(students);

        assertEquals(firstResult, secondResult);
    }

    @Test
    @DisplayName("Deve aceitar estudantes sem dados de competencias")
    void shouldAcceptStudentsWithoutCompetencyData() {

        List<StudentProfileEntry> students = createStudents(8);

        StudentProfile emptyProfile = new StudentProfile(
                0,
                0.0,
                List.of(
                        new CompetencyLevel(
                                "ORG",
                                null,
                                LevelOrigin.NO_DATA,
                                0.0,
                                null,
                                null,
                                List.of()
                        )
                )
        );

        students.set(
                0,
                new StudentProfileEntry(
                        1L,
                        "A001",
                        "Estudante 1",
                        emptyProfile
                )
        );

        List<List<StudentProfileEntry>> groups =
                groupFormation.distribute(students);

        assertEquals(2, groups.size());
        assertEquals(8, groups.stream().mapToInt(List::size).sum());
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

        return new StudentProfile(
                0,
                1.0,
                List.of(
                        competency("ORG", level),
                        competency("TEMPO", level),
                        competency("PROG", level),
                        competency("EQUIPA", level)
                )
        );
    }

    private CompetencyLevel competency(String code, Double level) {

        return new CompetencyLevel(
                code,
                level,
                LevelOrigin.QUIZ,
                1.0,
                level,
                null,
                List.of()
        );
    }
}

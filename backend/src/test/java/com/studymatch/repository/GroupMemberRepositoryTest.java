
package com.studymatch.repository;

import com.studymatch.model.GroupContext;
import com.studymatch.model.GroupMember;
import com.studymatch.model.Student;
import com.studymatch.model.StudentGroup;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class GroupMemberRepositoryTest {

    @Autowired
    private GroupMemberRepository repository;

    @Autowired
    private GroupContextRepository contextRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StudentGroupRepository groupRepository;

    @Test
    void shouldFindMembersByGroupOrderedByStudentNumber() {
        // Arrange
        GroupContext context = contextRepository.save(
                new GroupContext("Turma de Teste")
        );

        Student student2 = studentRepository.save(
                new Student(
                        "TEST002",
                        "Aluno B",
                        "teste002@example.com",
                        "Engenharia Informatica",
                        3
                )
        );

        Student student1 = studentRepository.save(
                new Student(
                        "TEST001",
                        "Aluno A",
                        "teste001@example.com",
                        "Engenharia Informatica",
                        3
                )
        );

        StudentGroup group = groupRepository.save(
                new StudentGroup(context, 1, 1, 14.5, 0.3)
        );

        repository.save(new GroupMember(
                group, student2, context.getId(), 1
        ));

        repository.save(new GroupMember(
                group, student1, context.getId(), 1
        ));

        // Act
        List<GroupMember> result =
                repository.findByGroup_IdOrderByStudent_StudentNumberAsc(
                        group.getId()
                );

        // Assert
        assertEquals(2, result.size());
        assertEquals("TEST001", result.get(0).getStudent().getStudentNumber());
        assertEquals("TEST002", result.get(1).getStudent().getStudentNumber());
    }

    @Test
    void shouldFindMembersByContextAndGeneration() {
        // Arrange
        GroupContext context = contextRepository.save(
                new GroupContext("Outra Turma")
        );

        Student student = studentRepository.save(
                new Student(
                        "TEST003",
                        "Aluno C",
                        "teste003@example.com",
                        "Engenharia Informatica",
                        2
                )
        );

        StudentGroup group = groupRepository.save(
                new StudentGroup(context, 1, 1, 15.0, 0.2)
        );

        repository.save(new GroupMember(
                group, student, context.getId(), 1
        ));

        // Act
        List<GroupMember> result =
                repository.findByContextIdAndGenerationNumber(
                        context.getId(),
                        1
                );

        // Assert
        assertEquals(1, result.size());
        assertEquals(student.getId(), result.get(0).getStudent().getId());
        assertEquals(1, result.get(0).getGenerationNumber());
    }

    @Test
    void shouldCheckIfStudentExistsInGeneration() {
        // Arrange
        GroupContext context = contextRepository.save(
                new GroupContext("Turma de Existencia")
        );

        Student student = studentRepository.save(
                new Student(
                        "TEST004",
                        "Aluno D",
                        "teste004@example.com",
                        "Engenharia Informatica",
                        1
                )
        );

        StudentGroup group = groupRepository.save(
                new StudentGroup(context, 1, 1, 13.0, 0.4)
        );

        repository.save(new GroupMember(
                group, student, context.getId(), 1
        ));

        // Act
        boolean exists =
                repository.existsByContextIdAndGenerationNumberAndStudent_Id(
                        context.getId(),
                        1,
                        student.getId()
                );

        // Assert
        assertTrue(exists);
        assertFalse(
                repository.existsByContextIdAndGenerationNumberAndStudent_Id(
                        context.getId(),
                        2,
                        student.getId()
                )
        );
    }
}

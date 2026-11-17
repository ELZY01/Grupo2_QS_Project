
package com.studymatch.repository;

import com.studymatch.model.GroupContext;
import com.studymatch.model.GroupContextStudent;
import com.studymatch.model.Student;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class GroupContextStudentRepositoryTest {

    @Autowired
    private GroupContextStudentRepository repository;

    @Autowired
    private GroupContextRepository contextRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Test
    void shouldSaveAndFindStudentsByContext() {
        // Arrange
        GroupContext context = contextRepository.save(
                new GroupContext("Turma de Teste")
        );

        Student student = studentRepository.save(
                new Student(
                        "TEST001",
                        "Aluno Teste",
                        "teste001@example.com",
                        "Engenharia Informatica",
                        3
                )
        );

        GroupContextStudent association =
                new GroupContextStudent(context, student);

        // Act
        repository.save(association);

        List<GroupContextStudent> result =
                repository.findByContext_Id(context.getId());

        // Assert
        assertEquals(1, result.size());
        assertEquals(student.getId(), result.get(0).getStudent().getId());
        assertEquals(context.getId(), result.get(0).getContext().getId());
    }

    @Test
    void shouldCheckIfStudentExistsInContext() {
        // Arrange
        GroupContext context = contextRepository.save(
                new GroupContext("Outra Turma")
        );

        Student student = studentRepository.save(
                new Student(
                        "TEST002",
                        "Outro Aluno",
                        "teste002@example.com",
                        "Engenharia Informatica",
                        2
                )
        );

        repository.save(new GroupContextStudent(context, student));

        // Act
        boolean exists = repository.existsByContext_IdAndStudent_Id(
                context.getId(),
                student.getId()
        );

        // Assert
        assertTrue(exists);
    }
}

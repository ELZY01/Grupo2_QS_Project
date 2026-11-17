
package com.studymatch.repository;

import com.studymatch.model.GroupContext;
import com.studymatch.model.StudentGroup;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class StudentGroupRepositoryTest {

    @Autowired
    private StudentGroupRepository repository;

    @Autowired
    private GroupContextRepository contextRepository;

    @Test
    void shouldFindGroupsByContextAndGeneration() {
        // Arrange
        GroupContext context = contextRepository.save(
                new GroupContext("Turma de Teste")
        );

        repository.save(new StudentGroup(
                context, 1, 2, 14.5, 0.3
        ));

        repository.save(new StudentGroup(
                context, 1, 1, 15.0, 0.2
        ));

        repository.save(new StudentGroup(
                context, 2, 1, 16.0, 0.1
        ));

        // Act
        List<StudentGroup> result =
                repository.findByContext_IdAndGenerationNumberOrderByGroupNumberAsc(
                        context.getId(),
                        1
                );

        // Assert
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getGroupNumber());
        assertEquals(2, result.get(1).getGroupNumber());
        assertEquals(1, result.get(0).getGenerationNumber());
    }

    @Test
    void shouldFindAllGenerationsOrderedByGenerationAndGroup() {
        // Arrange
        GroupContext context = contextRepository.save(
                new GroupContext("Outra Turma")
        );

        repository.save(new StudentGroup(
                context, 1, 2, 14.0, 0.3
        ));

        repository.save(new StudentGroup(
                context, 2, 1, 15.0, 0.2
        ));

        repository.save(new StudentGroup(
                context, 1, 1, 13.0, 0.4
        ));

        // Act
        List<StudentGroup> result =
                repository.findByContext_IdOrderByGenerationNumberDescGroupNumberAsc(
                        context.getId()
                );

        // Assert
        assertEquals(3, result.size());
        assertEquals(2, result.get(0).getGenerationNumber());
        assertEquals(1, result.get(1).getGenerationNumber());
        assertEquals(1, result.get(1).getGroupNumber());
        assertEquals(2, result.get(2).getGroupNumber());
    }

    @Test
    void shouldCheckIfGenerationExists() {
        // Arrange
        GroupContext context = contextRepository.save(
                new GroupContext("Turma de Existencia")
        );

        repository.save(new StudentGroup(
                context, 1, 1, 15.0, 0.2
        ));

        // Act
        boolean exists = repository.existsByContext_IdAndGenerationNumber(
                context.getId(),
                1
        );

        // Assert
        assertTrue(exists);
        assertFalse(repository.existsByContext_IdAndGenerationNumber(
                context.getId(),
                2
        ));
    }
}

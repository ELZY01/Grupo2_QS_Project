
package com.studymatch.repository;

import com.studymatch.model.GroupContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class GroupContextRepositoryTest {

    @Autowired
    private GroupContextRepository groupContextRepository;

    @Test
    void shouldSaveAndFindGroupContext() {
        // Arrange
        GroupContext context = new GroupContext("Contexto de teste");

        // Act
        GroupContext saved = groupContextRepository.save(context);

        Optional<GroupContext> found =
                groupContextRepository.findById(saved.getId());

        // Assert
        assertTrue(found.isPresent());
        assertEquals("Contexto de teste", found.get().getName());
    }

    @Test
    void shouldDeleteGroupContext() {
        // Arrange
        GroupContext context = new GroupContext("Contexto para eliminar");

        GroupContext saved = groupContextRepository.save(context);

        // Act
        groupContextRepository.deleteById(saved.getId());

        // Assert
        assertFalse(groupContextRepository.existsById(saved.getId()));
    }
}

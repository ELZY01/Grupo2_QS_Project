
package com.studymatch.repository;

import com.studymatch.model.StudentGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para gerir os grupos gerados.
 */
@Repository
public interface StudentGroupRepository
        extends JpaRepository<StudentGroup, Long> {

    List<StudentGroup> findByContext_IdAndGenerationNumberOrderByGroupNumberAsc(
            Long contextId,
            Integer generationNumber
    );

    List<StudentGroup> findByContext_IdOrderByGenerationNumberDescGroupNumberAsc(
            Long contextId
    );

    boolean existsByContext_IdAndGenerationNumber(
            Long contextId,
            Integer generationNumber
    );
}

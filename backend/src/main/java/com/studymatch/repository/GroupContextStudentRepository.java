
package com.studymatch.repository;

import com.studymatch.model.GroupContextStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para gerir os estudantes associados
 * aos contextos de formacao.
 */
@Repository
public interface GroupContextStudentRepository
        extends JpaRepository<GroupContextStudent, Long> {

    List<GroupContextStudent> findByContext_Id(Long contextId);

    boolean existsByContext_IdAndStudent_Id(
            Long contextId,
            Long studentId
    );
}

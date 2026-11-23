package com.studymatch.repository;

import com.studymatch.model.GroupContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gerir os contextos de formacao de grupos.
 */
@Repository
public interface GroupContextRepository extends JpaRepository<GroupContext, Long> {

    /** Contextos de uma UC, do mais recente para o mais antigo (Issue #106). */
    List<GroupContext> findByCourseUnitIdOrderByCreatedAtDescIdDesc(Long courseUnitId);

    /** Contexto só se for desta UC (Issue #106). */
    Optional<GroupContext> findByIdAndCourseUnitId(Long id, Long courseUnitId);
}


package com.studymatch.repository;

import com.studymatch.model.GroupContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para gerir os contextos de formacao de grupos.
 */
@Repository
public interface GroupContextRepository extends JpaRepository<GroupContext, Long> {
}


package com.studymatch.repository;

import com.studymatch.model.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para gerir os membros dos grupos gerados.
 */
@Repository
public interface GroupMemberRepository
        extends JpaRepository<GroupMember, Long> {

    List<GroupMember> findByGroup_IdOrderByStudent_StudentNumberAsc(
            Long groupId
    );

    List<GroupMember> findByContextIdAndGenerationNumber(
            Long contextId,
            Integer generationNumber
    );

    boolean existsByContextIdAndGenerationNumberAndStudent_Id(
            Long contextId,
            Integer generationNumber,
            Long studentId
    );
}

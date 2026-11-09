package com.studymatch.repository;

import com.studymatch.model.AppUser;
import com.studymatch.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositório das contas de acesso.
 */
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsernameIgnoreCase(String username);

    Optional<AppUser> findByStudentId(Long studentId);

    boolean existsByUsernameIgnoreCase(String username);

    List<AppUser> findByRoleOrderByDisplayNameAsc(Role role);
}

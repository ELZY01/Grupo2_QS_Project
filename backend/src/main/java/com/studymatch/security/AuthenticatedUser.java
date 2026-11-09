package com.studymatch.security;

import com.studymatch.model.AppUser;
import com.studymatch.model.Role;

/**
 * Utilizador com sessão iniciada (principal do Spring Security).
 *
 * @param id          ID da conta
 * @param username    nome de utilizador
 * @param role        perfil (ADMIN, DOCENTE ou ALUNO)
 * @param studentId   estudante associado (só no ALUNO)
 * @param displayName nome a mostrar
 */
public record AuthenticatedUser(Long id, String username, Role role, Long studentId, String displayName) {

    public static AuthenticatedUser from(AppUser user) {
        return new AuthenticatedUser(
                user.getId(), user.getUsername(), user.getRole(), user.getStudentId(), user.getDisplayName());
    }

    public boolean is(Role other) {
        return role == other;
    }
}

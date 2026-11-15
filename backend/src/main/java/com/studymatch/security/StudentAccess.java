package com.studymatch.security;

import com.studymatch.model.Role;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Regras de acesso aos dados de um estudante (RN05, RN16).
 *
 * - ADMIN vê e altera tudo.
 * - ALUNO só vê e altera os seus próprios dados.
 * - DOCENTE: ver {@link #canView} (alunos inscritos nas UCs do docente).
 */
@Component
public class StudentAccess {

    private final TeachingAccess teachingAccess;

    public StudentAccess(TeachingAccess teachingAccess) {
        this.teachingAccess = teachingAccess;
    }

    /**
     * Utilizador autenticado, ou null quando o pedido é anónimo.
     */
    public static AuthenticatedUser currentUser(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser user) {
            return user;
        }
        return null;
    }

    /**
     * Pode consultar o estudante (dados, perfil, histórico, resultados do quiz)?
     */
    public boolean canView(AuthenticatedUser user, Long studentId) {
        if (user == null || studentId == null) {
            return false;
        }
        return switch (user.role()) {
            case ADMIN -> true;
            case ALUNO -> isSelf(user, studentId);
            case DOCENTE -> teachingAccess.teachesStudent(user.id(), studentId);
        };
    }

    /**
     * Pode alterar os dados pessoais do estudante?
     */
    public boolean canEdit(AuthenticatedUser user, Long studentId) {
        return user != null && (user.is(Role.ADMIN) || isSelf(user, studentId));
    }

    /**
     * É o próprio aluno?
     */
    public boolean isSelf(AuthenticatedUser user, Long studentId) {
        return user != null && user.is(Role.ALUNO) && studentId != null && studentId.equals(user.studentId());
    }
}

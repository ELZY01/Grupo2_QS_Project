package com.studymatch.model;

/**
 * Perfis de utilizador do StudyMatch.
 *
 * ADMIN: gere estudantes, UCs, notas e as atribuições de docentes e alunos.
 * DOCENTE: vê as suas UCs, os alunos inscritos e as estatísticas.
 * ALUNO: acede apenas aos seus próprios dados (RN05).
 */
public enum Role {
    ADMIN,
    DOCENTE,
    ALUNO;

    /**
     * Nome da authority usada pelo Spring Security (ex.: ROLE_ADMIN).
     */
    public String authority() {
        return "ROLE_" + name();
    }
}

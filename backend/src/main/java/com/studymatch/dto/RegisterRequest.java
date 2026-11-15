package com.studymatch.dto;

/**
 * Registo de um novo aluno, já com a palavra-passe da conta.
 */
public record RegisterRequest(
        String studentNumber,
        String name,
        String email,
        String course,
        Integer academicYear,
        String password
) {
}

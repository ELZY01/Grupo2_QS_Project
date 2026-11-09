package com.studymatch.dto;

import com.studymatch.model.Role;

/**
 * Resposta do login e do registo.
 *
 * @param token       token a enviar em "Authorization: Bearer ..." nos pedidos seguintes
 * @param role        perfil do utilizador
 * @param userId      ID da conta
 * @param studentId   ID do estudante (só no aluno)
 * @param displayName nome a mostrar
 */
public record LoginResponse(String token, Role role, Long userId, Long studentId, String displayName) {
}

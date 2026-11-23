package com.studymatch.dto;

import java.util.List;

/**
 * Membros que um grupo vai ter depois de o docente o criar ou editar (Issue #106).
 *
 * @param studentIds IDs dos alunos (4 a 5, inscritos na UC e sem outro grupo no contexto)
 */
public record GroupMembersRequest(List<Long> studentIds) {
}

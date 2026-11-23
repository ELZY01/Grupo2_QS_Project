package com.studymatch.grouping;

/**
 * A alteração de um grupo viola uma regra de composição (Issue #106, R4 e R5):
 * aluno não inscrito na UC, aluno já noutro grupo do mesmo contexto, aluno repetido
 * ou tamanho fora dos limites.
 *
 * <p>O service dos grupos responde 409 com a mensagem desta exceção.
 */
public class GroupMembershipConflictException extends RuntimeException {

    public GroupMembershipConflictException(String message) {
        super(message);
    }
}

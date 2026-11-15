package com.studymatch.grouping;

/**
 * Não é possível dividir os alunos em grupos de 4 ou 5 (Issue #73, ADR-003 D1).
 *
 * <p>Acontece com 0, 1, 2, 3, 6, 7 ou 11 alunos, ou com alunos repetidos na entrada.
 * A API responde 400 com a mensagem desta exceção.
 */
public class ImpossibleGroupConfigurationException extends IllegalArgumentException {

    public ImpossibleGroupConfigurationException(String message) {
        super(message);
    }
}

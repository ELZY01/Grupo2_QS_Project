package com.studymatch.grouping;

/**
 * O docente tentou gerir grupos de uma UC que não é sua (Issue #106, R4).
 *
 * <p>O service dos grupos responde 403 com a mensagem desta exceção.
 */
public class GroupAccessDeniedException extends RuntimeException {

    public GroupAccessDeniedException(String message) {
        super(message);
    }
}

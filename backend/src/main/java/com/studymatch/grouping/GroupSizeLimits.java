package com.studymatch.grouping;

/**
 * Tamanho mínimo e máximo dos grupos de um contexto (Issue #106, R5).
 *
 * <p>Por omissão são 4 a 5 alunos ({@link #DEFAULT}), como no enunciado do Sprint 3,
 * mas cada contexto pode ter os seus limites.
 *
 * @param min nº mínimo de alunos por grupo (pelo menos 1)
 * @param max nº máximo de alunos por grupo (pelo menos {@code min})
 */
public record GroupSizeLimits(int min, int max) {

    public static final GroupSizeLimits DEFAULT =
            new GroupSizeLimits(GroupFormation.MIN_GROUP_SIZE, GroupFormation.MAX_GROUP_SIZE);

    public GroupSizeLimits {
        if (min < 1) {
            throw new IllegalArgumentException("Um grupo tem de ter pelo menos 1 aluno.");
        }
        if (max < min) {
            throw new IllegalArgumentException(
                    "O tamanho máximo (" + max + ") não pode ser menor do que o mínimo (" + min + ").");
        }
    }

    /** {@code true} se um grupo com {@code size} alunos respeita os limites. */
    public boolean allows(int size) {
        return size >= min && size <= max;
    }
}

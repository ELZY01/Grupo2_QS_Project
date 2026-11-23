package com.studymatch.grouping;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Regras para criar e editar grupos à mão numa UC (Issue #106, R4 e R5).
 *
 * <p>Java puro, sem Spring nem base de dados: o service dos grupos (#69) vai buscar os dados
 * aos repositórios e chama estas regras antes de guardar. Assim as regras são testadas sozinhas
 * e o service não fica acoplado à UI nem ao algoritmo de formação.
 *
 * <p>Ordem no service: primeiro {@link #requireTeacherOfUnit} (403) e depois
 * {@link #validateMembers} (409).
 */
public final class GroupMembershipRules {

    private GroupMembershipRules() {
    }

    /**
     * R4: o docente só gere grupos das UCs a que está associado.
     *
     * @param teacherUnitIds UCs do docente
     * @param courseUnitId   UC do contexto de agrupamento
     * @throws GroupAccessDeniedException se a UC não é do docente (403)
     */
    public static void requireTeacherOfUnit(Set<Long> teacherUnitIds, Long courseUnitId) {
        Objects.requireNonNull(teacherUnitIds, "As UCs do docente são obrigatórias.");
        if (courseUnitId == null || !teacherUnitIds.contains(courseUnitId)) {
            throw new GroupAccessDeniedException("Só pode gerir grupos das suas unidades curriculares.");
        }
    }

    /**
     * Valida a lista final de membros de um grupo (criado ou editado).
     *
     * @param studentIds     membros que o grupo vai ter depois da alteração
     * @param enrolledIds    alunos inscritos na UC (R4)
     * @param groupOfStudent grupo atual de cada aluno no mesmo contexto (aluno → nº do grupo)
     * @param groupNumber    nº do grupo que está a ser editado, ou {@code null} se é um grupo novo
     * @param limits         tamanho permitido neste contexto (R5)
     * @throws GroupMembershipConflictException se alguma regra falha (409)
     */
    public static void validateMembers(Collection<Long> studentIds, Set<Long> enrolledIds,
                                       Map<Long, Integer> groupOfStudent, Integer groupNumber,
                                       GroupSizeLimits limits) {
        Objects.requireNonNull(studentIds, "A lista de alunos do grupo é obrigatória.");
        Objects.requireNonNull(enrolledIds, "Os alunos inscritos na UC são obrigatórios.");
        Objects.requireNonNull(groupOfStudent, "Os grupos atuais do contexto são obrigatórios.");
        Objects.requireNonNull(limits, "Os limites de tamanho são obrigatórios.");

        requireNoRepeated(studentIds);
        requireEnrolled(studentIds, enrolledIds);
        requireNotInAnotherGroup(studentIds, groupOfStudent, groupNumber);
        requireSize(studentIds.size(), limits);
    }

    /** O mesmo aluno não pode aparecer duas vezes na lista (nem ser {@code null}). */
    public static void requireNoRepeated(Collection<Long> studentIds) {
        Set<Long> seen = new HashSet<>();
        for (Long id : studentIds) {
            if (id == null) {
                throw new GroupMembershipConflictException("A lista de alunos não pode ter valores vazios.");
            }
            if (!seen.add(id)) {
                throw new GroupMembershipConflictException("O aluno " + id + " aparece mais de uma vez no grupo.");
            }
        }
    }

    /** R4: só alunos inscritos na UC. */
    public static void requireEnrolled(Collection<Long> studentIds, Set<Long> enrolledIds) {
        for (Long id : studentIds) {
            if (!enrolledIds.contains(id)) {
                throw new GroupMembershipConflictException("O aluno " + id + " não está inscrito nesta UC.");
            }
        }
    }

    /** R5: um aluno não pode estar em dois grupos do mesmo contexto. */
    static void requireNotInAnotherGroup(Collection<Long> studentIds, Map<Long, Integer> groupOfStudent,
                                         Integer groupNumber) {
        for (Long id : studentIds) {
            Integer current = groupOfStudent.get(id);
            if (current != null && !current.equals(groupNumber)) {
                throw new GroupMembershipConflictException(
                        "O aluno " + id + " já está no grupo " + current + " deste contexto.");
            }
        }
    }

    /** R5: tamanho dentro dos limites do contexto (4 a 5 por omissão). */
    static void requireSize(int size, GroupSizeLimits limits) {
        if (!limits.allows(size)) {
            throw new GroupMembershipConflictException(
                    "Um grupo tem de ter entre " + limits.min() + " e " + limits.max()
                            + " alunos (este teria " + size + ").");
        }
    }
}

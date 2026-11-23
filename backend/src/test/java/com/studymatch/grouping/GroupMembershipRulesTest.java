package com.studymatch.grouping;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Testes das regras de criação e edição manual de grupos (Issue #106, R4 e R5).
 */
@DisplayName("Regras dos grupos de uma UC")
class GroupMembershipRulesTest {

    private static final Long UC = 10L;
    private static final Set<Long> ENROLLED = Set.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L);

    @Test
    @DisplayName("R4: o docente gere grupos das suas UCs")
    void aTeacherManagesGroupsOfTheirUnit() {
        assertDoesNotThrow(() -> GroupMembershipRules.requireTeacherOfUnit(Set.of(10L, 11L), UC));
    }

    @Test
    @DisplayName("R4: UC de outro docente ou UC em falta dá 403")
    void aTeacherCannotManageGroupsOfAnotherUnit() {
        GroupAccessDeniedException error = assertThrows(GroupAccessDeniedException.class,
                () -> GroupMembershipRules.requireTeacherOfUnit(Set.of(11L), UC));
        assertEquals("Só pode gerir grupos das suas unidades curriculares.", error.getMessage());

        assertThrows(GroupAccessDeniedException.class,
                () -> GroupMembershipRules.requireTeacherOfUnit(Set.of(11L), null));
    }

    @Test
    @DisplayName("Grupo novo de 4 e de 5 alunos inscritos e sem grupo é aceite")
    void aValidNewGroupIsAccepted() {
        assertDoesNotThrow(() -> GroupMembershipRules.validateMembers(
                List.of(1L, 2L, 3L, 4L), ENROLLED, Map.of(), null, GroupSizeLimits.DEFAULT));
        assertDoesNotThrow(() -> GroupMembershipRules.validateMembers(
                List.of(1L, 2L, 3L, 4L, 5L), ENROLLED, Map.of(), null, GroupSizeLimits.DEFAULT));
    }

    @Test
    @DisplayName("R4: aluno não inscrito na UC dá 409")
    void aStudentNotEnrolledIsRejected() {
        GroupMembershipConflictException error = assertThrows(GroupMembershipConflictException.class,
                () -> GroupMembershipRules.validateMembers(
                        List.of(1L, 2L, 3L, 99L), ENROLLED, Map.of(), null, GroupSizeLimits.DEFAULT));
        assertEquals("O aluno 99 não está inscrito nesta UC.", error.getMessage());
    }

    @Test
    @DisplayName("R5: aluno que já está noutro grupo do mesmo contexto dá 409")
    void aStudentAlreadyInAnotherGroupIsRejected() {
        Map<Long, Integer> current = Map.of(1L, 1, 2L, 1, 3L, 1, 4L, 1);

        GroupMembershipConflictException error = assertThrows(GroupMembershipConflictException.class,
                () -> GroupMembershipRules.validateMembers(
                        List.of(4L, 5L, 6L, 7L), ENROLLED, current, null, GroupSizeLimits.DEFAULT));
        assertEquals("O aluno 4 já está no grupo 1 deste contexto.", error.getMessage());

        assertThrows(GroupMembershipConflictException.class,
                () -> GroupMembershipRules.validateMembers(
                        List.of(4L, 5L, 6L, 7L), ENROLLED, current, 2, GroupSizeLimits.DEFAULT));
    }

    @Test
    @DisplayName("Editar um grupo mantendo os seus próprios membros é aceite")
    void editingAGroupKeepsItsOwnMembers() {
        Map<Long, Integer> current = Map.of(1L, 1, 2L, 1, 3L, 1, 4L, 1, 5L, 2);

        assertDoesNotThrow(() -> GroupMembershipRules.validateMembers(
                List.of(1L, 2L, 3L, 4L, 6L), ENROLLED, current, 1, GroupSizeLimits.DEFAULT));
    }

    @Test
    @DisplayName("Aluno repetido na lista ou valor vazio dá 409")
    void repeatedOrNullStudentsAreRejected() {
        GroupMembershipConflictException error = assertThrows(GroupMembershipConflictException.class,
                () -> GroupMembershipRules.validateMembers(
                        List.of(1L, 2L, 3L, 3L), ENROLLED, Map.of(), null, GroupSizeLimits.DEFAULT));
        assertEquals("O aluno 3 aparece mais de uma vez no grupo.", error.getMessage());

        assertThrows(GroupMembershipConflictException.class,
                () -> GroupMembershipRules.validateMembers(
                        Arrays.asList(1L, 2L, null, 4L), ENROLLED, Map.of(), null, GroupSizeLimits.DEFAULT));
    }

    @Test
    @DisplayName("R5: grupos de 3 ou de 6 alunos dão 409 com os limites 4–5")
    void sizesOutsideTheDefaultLimitsAreRejected() {
        GroupMembershipConflictException tooSmall = assertThrows(GroupMembershipConflictException.class,
                () -> GroupMembershipRules.validateMembers(
                        List.of(1L, 2L, 3L), ENROLLED, Map.of(), null, GroupSizeLimits.DEFAULT));
        assertEquals("Um grupo tem de ter entre 4 e 5 alunos (este teria 3).", tooSmall.getMessage());

        assertThrows(GroupMembershipConflictException.class,
                () -> GroupMembershipRules.validateMembers(
                        List.of(1L, 2L, 3L, 4L, 5L, 6L), ENROLLED, Map.of(), null, GroupSizeLimits.DEFAULT));
    }

    @Test
    @DisplayName("R5: o tamanho é configurável por contexto")
    void sizeIsConfigurablePerContext() {
        GroupSizeLimits pairs = new GroupSizeLimits(2, 3);

        assertDoesNotThrow(() -> GroupMembershipRules.validateMembers(
                List.of(1L, 2L), ENROLLED, Map.of(), null, pairs));
        assertThrows(GroupMembershipConflictException.class,
                () -> GroupMembershipRules.validateMembers(
                        List.of(1L, 2L, 3L, 4L), ENROLLED, Map.of(), null, pairs));
    }

    @Test
    @DisplayName("Limites inválidos são recusados; os de omissão são 4 e 5")
    void invalidLimitsAreRejected() {
        assertEquals(4, GroupSizeLimits.DEFAULT.min());
        assertEquals(5, GroupSizeLimits.DEFAULT.max());
        assertTrue(GroupSizeLimits.DEFAULT.allows(4));
        assertFalse(GroupSizeLimits.DEFAULT.allows(6));

        assertThrows(IllegalArgumentException.class, () -> new GroupSizeLimits(0, 5));
        assertThrows(IllegalArgumentException.class, () -> new GroupSizeLimits(5, 4));
    }

    @Test
    @DisplayName("Dados em falta dão erro de programação, não 409")
    void missingInputsAreProgrammingErrors() {
        assertThrows(NullPointerException.class, () -> GroupMembershipRules.requireTeacherOfUnit(null, UC));
        assertThrows(NullPointerException.class, () -> GroupMembershipRules.validateMembers(
                null, ENROLLED, Map.of(), null, GroupSizeLimits.DEFAULT));
        assertThrows(NullPointerException.class, () -> GroupMembershipRules.validateMembers(
                List.of(1L), ENROLLED, Map.of(), null, null));
    }
}

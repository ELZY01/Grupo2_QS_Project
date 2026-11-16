package com.studymatch.security;

import com.studymatch.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Testes unitários das regras de acesso aos dados de um estudante (RN05, RN16).
 */
class StudentAccessTest {

    private TeachingAccess teachingAccess;
    private StudentAccess access;

    private final AuthenticatedUser admin = new AuthenticatedUser(1L, "admin", Role.ADMIN, null, "Admin");
    private final AuthenticatedUser ana = new AuthenticatedUser(2L, "a1", Role.ALUNO, 7L, "Ana");
    private final AuthenticatedUser rita = new AuthenticatedUser(3L, "rita", Role.DOCENTE, null, "Prof. Rita");

    @BeforeEach
    void setUp() {
        teachingAccess = mock(TeachingAccess.class);
        access = new StudentAccess(teachingAccess);
    }

    // --- Utilizador atual -------------------------------------------------

    @Test
    void currentUserIsNullWithoutASession() {
        assertNull(StudentAccess.currentUser(null));
    }

    @Test
    void currentUserIsNullWhenThePrincipalIsNotAnAuthenticatedUser() {
        var anonymous = new UsernamePasswordAuthenticationToken("anonimo", null, List.of());

        assertNull(StudentAccess.currentUser(anonymous));
    }

    @Test
    void currentUserReturnsTheLoggedInUser() {
        var session = new UsernamePasswordAuthenticationToken(ana, null, List.of());

        assertSame(ana, StudentAccess.currentUser(session));
    }

    // --- Ver dados --------------------------------------------------------

    @Test
    void nobodySeesAStudentWithoutASessionOrWithoutAnId() {
        assertFalse(access.canView(null, 7L));
        assertFalse(access.canView(admin, null));
        verifyNoInteractions(teachingAccess);
    }

    @Test
    void theAdminSeesEveryStudent() {
        assertTrue(access.canView(admin, 7L));
        assertTrue(access.canView(admin, 8L));
    }

    @Test
    void aStudentOnlySeesTheirOwnData() {
        assertTrue(access.canView(ana, 7L));
        assertFalse(access.canView(ana, 8L));
    }

    @Test
    void aTeacherOnlySeesStudentsOfTheirUnits() {
        when(teachingAccess.teachesStudent(3L, 7L)).thenReturn(true);
        when(teachingAccess.teachesStudent(3L, 8L)).thenReturn(false);

        assertTrue(access.canView(rita, 7L));
        assertFalse(access.canView(rita, 8L));
    }

    // --- Editar dados -----------------------------------------------------

    @Test
    void onlyTheAdminAndTheStudentThemselvesCanEdit() {
        assertTrue(access.canEdit(admin, 7L));
        assertTrue(access.canEdit(ana, 7L));
        assertFalse(access.canEdit(ana, 8L));
        assertFalse(access.canEdit(null, 7L));
    }

    @Test
    void aTeacherCannotEditEvenAStudentOfTheirUnit() {
        assertFalse(access.canEdit(rita, 7L));
        verifyNoInteractions(teachingAccess);
    }

    // --- É o próprio aluno? -----------------------------------------------

    @Test
    void isSelfOnlyForTheStudentWithTheSameId() {
        assertTrue(access.isSelf(ana, 7L));
        assertFalse(access.isSelf(ana, null));
        assertFalse(access.isSelf(null, 7L));
        assertFalse(access.isSelf(admin, 7L));
    }
}

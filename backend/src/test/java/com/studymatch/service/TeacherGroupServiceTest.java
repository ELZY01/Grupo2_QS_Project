package com.studymatch.service;

import com.studymatch.dto.CreateGroupContextRequest;
import com.studymatch.dto.GroupMembersRequest;
import com.studymatch.dto.TeacherGroupsResponse;
import com.studymatch.grouping.GroupAccessDeniedException;
import com.studymatch.grouping.GroupMembershipConflictException;
import com.studymatch.model.AppUser;
import com.studymatch.model.Competency;
import com.studymatch.model.CourseUnit;
import com.studymatch.model.CourseUnitStudent;
import com.studymatch.model.CourseUnitTeacher;
import com.studymatch.model.GroupContext;
import com.studymatch.model.GroupContextStudent;
import com.studymatch.model.GroupMember;
import com.studymatch.model.Role;
import com.studymatch.model.Student;
import com.studymatch.model.StudentGroup;
import com.studymatch.profile.CompetencyLevel;
import com.studymatch.profile.LevelOrigin;
import com.studymatch.profile.StudentProfile;
import com.studymatch.repository.CompetencyRepository;
import com.studymatch.repository.CourseUnitRepository;
import com.studymatch.repository.CourseUnitStudentRepository;
import com.studymatch.repository.CourseUnitTeacherRepository;
import com.studymatch.repository.GroupContextRepository;
import com.studymatch.repository.GroupContextStudentRepository;
import com.studymatch.repository.GroupMemberRepository;
import com.studymatch.repository.StudentGroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Grupos de uma UC na área do docente (Issue #106), com Mockito: acesso (R4), regras dos
 * membros (R5) e estatísticas (R6). O fluxo completo com base de dados está no TeacherGroupFlowTest.
 */
class TeacherGroupServiceTest {

    private static final long TEACHER = 3L;
    private static final long UNIT = 10L;
    private static final long CONTEXT = 20L;

    private CourseUnitRepository courseUnits;
    private CourseUnitTeacherRepository teachers;
    private CourseUnitStudentRepository enrolled;
    private GroupContextRepository contexts;
    private GroupContextStudentRepository contextStudents;
    private StudentGroupRepository groups;
    private GroupMemberRepository members;
    private CompetencyRepository competencies;
    private ProfileService profiles;
    private GroupGenerationService generation;
    private TeacherGroupService service;

    private final CourseUnit qs = withId(new CourseUnit("QS", "Qualidade de Software", 6, 3, 1), UNIT);
    private final AppUser rita = withId(new AppUser("rita", "hash", Role.DOCENTE, "Prof. Rita", null), TEACHER);
    private final GroupContext context = withId(new GroupContext("Projeto final", UNIT), CONTEXT);

    /** Alunos 1 a 9, inscritos na UC. */
    private final List<Student> students = new ArrayList<>();

    @BeforeEach
    void setUp() {
        courseUnits = mock(CourseUnitRepository.class);
        teachers = mock(CourseUnitTeacherRepository.class);
        enrolled = mock(CourseUnitStudentRepository.class);
        contexts = mock(GroupContextRepository.class);
        contextStudents = mock(GroupContextStudentRepository.class);
        groups = mock(StudentGroupRepository.class);
        members = mock(GroupMemberRepository.class);
        competencies = mock(CompetencyRepository.class);
        profiles = mock(ProfileService.class);
        generation = mock(GroupGenerationService.class);
        service = new TeacherGroupService(courseUnits, teachers, enrolled, contexts, contextStudents, groups,
                members, competencies, profiles, generation);

        when(courseUnits.existsById(UNIT)).thenReturn(true);
        when(teachers.findByTeacherIdOrderByCourseUnitNameAsc(TEACHER))
                .thenReturn(List.of(new CourseUnitTeacher(qs, rita)));
        when(contexts.findByIdAndCourseUnitId(CONTEXT, UNIT)).thenReturn(Optional.of(context));

        for (long id = 1; id <= 9; id++) {
            students.add(withId(new Student("a" + id, "Aluno " + id, "a" + id + "@ulusofona.pt", "LEI", 3), id));
        }
        when(enrolled.findByCourseUnitIdOrderByStudentNameAsc(UNIT))
                .thenReturn(students.stream().map(s -> new CourseUnitStudent(qs, s)).toList());
        when(contextStudents.findByContext_Id(CONTEXT))
                .thenReturn(students.stream().map(s -> new GroupContextStudent(context, s)).toList());
        when(competencies.findAllByOrderByIdAsc()).thenReturn(List.of(
                new Competency("ORG", "Organização", "", true),
                new Competency("PROG", "Programação", "", true)));
    }

    // ---------------------------------------------------------------------
    // Acesso (R4)
    // ---------------------------------------------------------------------

    @Test
    void listsTheContextsOfTheUnitWithTheStudentCountAndTheGeneration() {
        when(contexts.findByCourseUnitIdOrderByCreatedAtDescIdDesc(UNIT)).thenReturn(List.of(context));
        when(groups.findByContext_IdOrderByGenerationNumberDescGroupNumberAsc(CONTEXT))
                .thenReturn(List.of(group(31L, 2, 1)));

        var list = service.contexts(TEACHER, UNIT);

        assertEquals(1, list.size());
        assertEquals("Projeto final", list.get(0).name());
        assertEquals(UNIT, list.get(0).courseUnitId());
        assertEquals(9, list.get(0).studentCount());
        assertEquals(2, list.get(0).generationNumber());
    }

    @Test
    void aUnitThatDoesNotExistGives404() {
        when(courseUnits.existsById(99L)).thenReturn(false);

        var error = assertThrows(ResponseStatusException.class, () -> service.contexts(TEACHER, 99L));

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
    }

    @Test
    void aTeacherCannotManageGroupsOfAnotherUnit() {
        when(teachers.findByTeacherIdOrderByCourseUnitNameAsc(4L)).thenReturn(List.of());

        var error = assertThrows(GroupAccessDeniedException.class,
                () -> service.createGroup(4L, UNIT, CONTEXT, new GroupMembersRequest(List.of(1L, 2L, 3L, 4L))));

        assertEquals("Só pode gerir grupos das suas unidades curriculares.", error.getMessage());
        verify(contexts, never()).findByIdAndCourseUnitId(anyLong(), anyLong());
        verify(members, never()).saveAll(any());
    }

    @Test
    void aContextOfAnotherUnitIsNotFound() {
        when(contexts.findByIdAndCourseUnitId(77L, UNIT)).thenReturn(Optional.empty());

        var error = assertThrows(ResponseStatusException.class, () -> service.groups(TEACHER, UNIT, 77L));

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
    }

    // ---------------------------------------------------------------------
    // Contextos (US10)
    // ---------------------------------------------------------------------

    @Test
    void createsAContextInTheUnitWithTheEnrolledStudents() {
        when(contexts.save(any(GroupContext.class))).thenAnswer(call -> withId(call.getArgument(0), 21L));

        var created = service.createContext(TEACHER, UNIT,
                new CreateGroupContextRequest("  Projeto  ", List.of(1L, 2L, 3L, 4L, 5L)));

        assertEquals(21L, created.id());
        assertEquals("Projeto", created.name());
        assertEquals(UNIT, created.courseUnitId());
        assertEquals(5, created.studentCount());
        assertNull(created.generationNumber());
        verify(contextStudents).saveAll(any());
    }

    @Test
    void aContextNeedsANameAndStudents() {
        for (CreateGroupContextRequest request : List.of(
                new CreateGroupContextRequest(" ", List.of(1L)),
                new CreateGroupContextRequest(null, List.of(1L)),
                new CreateGroupContextRequest("Projeto", List.of()),
                new CreateGroupContextRequest("Projeto", null))) {
            var error = assertThrows(ResponseStatusException.class,
                    () -> service.createContext(TEACHER, UNIT, request));
            assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        }
        var error = assertThrows(ResponseStatusException.class, () -> service.createContext(TEACHER, UNIT, null));
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verify(contexts, never()).save(any());
    }

    @Test
    void aContextOnlyAcceptsEnrolledStudentsOnce() {
        var notEnrolled = assertThrows(GroupMembershipConflictException.class, () -> service.createContext(
                TEACHER, UNIT, new CreateGroupContextRequest("Projeto", List.of(1L, 99L))));
        var repeated = assertThrows(GroupMembershipConflictException.class, () -> service.createContext(
                TEACHER, UNIT, new CreateGroupContextRequest("Projeto", List.of(1L, 1L))));

        assertEquals("O aluno 99 não está inscrito nesta UC.", notEnrolled.getMessage());
        assertEquals("O aluno 1 aparece mais de uma vez no grupo.", repeated.getMessage());
        verify(contexts, never()).save(any());
    }

    // ---------------------------------------------------------------------
    // Geração (US11) e estatísticas (R6)
    // ---------------------------------------------------------------------

    @Test
    void generatingUsesTheAlgorithmAndReturnsTheStatistics() {
        givenGeneration(1);

        TeacherGroupsResponse response = service.generate(TEACHER, UNIT, CONTEXT);

        verify(generation).generateGroups(CONTEXT);
        assertEquals(1, response.generationNumber());
        assertEquals(2, response.groups().size());
    }

    @Test
    void withoutGroupsEveryStudentIsUnassigned() {
        when(groups.findByContext_IdOrderByGenerationNumberDescGroupNumberAsc(CONTEXT)).thenReturn(List.of());

        TeacherGroupsResponse response = service.groups(TEACHER, UNIT, CONTEXT);

        assertNull(response.generationNumber());
        assertTrue(response.groups().isEmpty());
        assertEquals(9, response.unassigned().size());
        assertEquals(0.5, response.maxAverageGap());
        assertTrue(response.balanced());
    }

    @Test
    void eachGroupHasItsStatistics() {
        givenGeneration(1);

        TeacherGroupsResponse response = service.groups(TEACHER, UNIT, CONTEXT);

        // Grupo 1: alunos 1–5 com ORG 4,0 e PROG 2,0 → média 3,0 por aluno.
        // Grupo 2: alunos 6–9 com ORG 2,0 e PROG 5,0 → média 3,5 por aluno.
        var g1 = response.groups().get(0);
        var g2 = response.groups().get(1);
        assertEquals(5, g1.memberCount());
        assertEquals(3.0, g1.average(), 1e-9);
        assertEquals(-0.5, g1.gapToOthers(), 1e-9);
        assertTrue(g1.balanced());
        assertEquals(List.of("PROG"), g1.gaps());
        assertEquals(4.0, g1.areas().get(0).average(), 1e-9);
        assertEquals("Aluno 1", g1.strongestByArea().get(0).studentName());
        assertEquals(4, g2.memberCount());
        assertEquals(List.of("ORG"), g2.gaps());
        assertEquals(0.5, response.averageSpread(), 1e-9);
        assertTrue(response.balanced());
        assertTrue(response.unassigned().isEmpty());
        verify(groups, never()).saveAll(any());
    }

    @Test
    void groupsAboveTheLimitAreNotBalanced() {
        StudentGroup g1 = group(31L, 1, 1);
        StudentGroup g2 = group(32L, 1, 2);
        when(groups.findByContext_IdOrderByGenerationNumberDescGroupNumberAsc(CONTEXT)).thenReturn(List.of(g1, g2));
        when(groups.findByContext_IdAndGenerationNumberOrderByGroupNumberAsc(CONTEXT, 1)).thenReturn(List.of(g1, g2));
        List<GroupMember> strong = members(g1, 1, 4, 5.0, 5.0);
        List<GroupMember> weak = members(g2, 5, 8, 1.0, 1.0);
        when(members.findByGroup_IdOrderByStudent_StudentNumberAsc(31L)).thenReturn(strong);
        when(members.findByGroup_IdOrderByStudent_StudentNumberAsc(32L)).thenReturn(weak);

        TeacherGroupsResponse response = service.groups(TEACHER, UNIT, CONTEXT);

        assertEquals(4.0, response.averageSpread(), 1e-9);
        assertFalse(response.balanced());
        assertFalse(response.groups().get(0).balanced());
    }

    @Test
    void withoutCompetenciesTheStatisticsCannotBeCalculated() {
        givenGeneration(1);
        when(competencies.findAllByOrderByIdAsc()).thenReturn(List.of());

        var error = assertThrows(ResponseStatusException.class, () -> service.groups(TEACHER, UNIT, CONTEXT));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
    }

    // ---------------------------------------------------------------------
    // Criar, editar e apagar grupos (R4, R5)
    // ---------------------------------------------------------------------

    @Test
    void aStudentAlreadyInAnotherGroupOfTheContextIsRejected() {
        givenGeneration(1);

        // O aluno 1 está no grupo 1; não pode entrar também no grupo 2.
        var error = assertThrows(GroupMembershipConflictException.class, () -> service.updateGroup(
                TEACHER, UNIT, CONTEXT, 32L, new GroupMembersRequest(List.of(1L, 6L, 7L, 8L, 9L))));

        assertEquals("O aluno 1 já está no grupo 1 deste contexto.", error.getMessage());
        verify(members, never()).saveAll(any());
        verify(members, never()).deleteAll(any());
    }

    @Test
    void aNewGroupWithStudentsThatAlreadyHaveAGroupIsRejected() {
        givenGeneration(1);

        var error = assertThrows(GroupMembershipConflictException.class, () -> service.createGroup(
                TEACHER, UNIT, CONTEXT, new GroupMembersRequest(List.of(6L, 7L, 8L, 9L))));

        assertEquals("O aluno 6 já está no grupo 2 deste contexto.", error.getMessage());
        verify(groups, never()).save(any());
    }

    @Test
    void aGroupWithAStudentNotEnrolledOrOutsideTheLimitsIsRejected() {
        when(groups.findByContext_IdOrderByGenerationNumberDescGroupNumberAsc(CONTEXT)).thenReturn(List.of());

        var notEnrolled = assertThrows(GroupMembershipConflictException.class, () -> service.createGroup(
                TEACHER, UNIT, CONTEXT, new GroupMembersRequest(List.of(1L, 2L, 3L, 99L))));
        var tooSmall = assertThrows(GroupMembershipConflictException.class, () -> service.createGroup(
                TEACHER, UNIT, CONTEXT, new GroupMembersRequest(List.of(1L, 2L, 3L))));

        assertEquals("O aluno 99 não está inscrito nesta UC.", notEnrolled.getMessage());
        assertEquals("Um grupo tem de ter entre 4 e 5 alunos (este teria 3).", tooSmall.getMessage());
        verify(groups, never()).save(any());
    }

    @Test
    void aGroupRequestNeedsTheListOfStudents() {
        var withoutBody = assertThrows(ResponseStatusException.class,
                () -> service.createGroup(TEACHER, UNIT, CONTEXT, null));
        var withoutList = assertThrows(ResponseStatusException.class,
                () -> service.createGroup(TEACHER, UNIT, CONTEXT, new GroupMembersRequest(null)));

        assertEquals(HttpStatus.BAD_REQUEST, withoutBody.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, withoutList.getStatusCode());
    }

    @Test
    void aGroupOfAnotherContextIsNotFound() {
        StudentGroup other = withId(new StudentGroup(withId(new GroupContext("Outro", UNIT), 88L), 1, 1, 3.0, 0.0), 50L);
        when(groups.findById(50L)).thenReturn(Optional.of(other));
        when(groups.findById(51L)).thenReturn(Optional.empty());

        var otherContext = assertThrows(ResponseStatusException.class,
                () -> service.deleteGroup(TEACHER, UNIT, CONTEXT, 50L));
        var missing = assertThrows(ResponseStatusException.class,
                () -> service.deleteGroup(TEACHER, UNIT, CONTEXT, 51L));

        assertEquals(HttpStatus.NOT_FOUND, otherContext.getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, missing.getStatusCode());
        verify(groups, never()).delete(any());
    }

    @Test
    void onlyTheLatestGenerationCanBeChanged() {
        givenGeneration(2);
        StudentGroup old = group(40L, 1, 1);
        when(groups.findById(40L)).thenReturn(Optional.of(old));

        var error = assertThrows(ResponseStatusException.class, () -> service.updateGroup(
                TEACHER, UNIT, CONTEXT, 40L, new GroupMembersRequest(List.of(1L, 2L, 3L, 4L))));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(members, never()).saveAll(any());
    }

    @Test
    void theFirstGroupStartsGeneration1AndAddsMissingStudentsToTheContext() {
        // Contexto só com os alunos 1–4; o aluno 5 está inscrito na UC mas ainda não é elegível.
        when(contextStudents.findByContext_Id(CONTEXT)).thenReturn(students.subList(0, 4).stream()
                .map(s -> new GroupContextStudent(context, s)).toList());
        when(groups.findByContext_IdOrderByGenerationNumberDescGroupNumberAsc(CONTEXT)).thenReturn(List.of());
        when(groups.save(any(StudentGroup.class))).thenAnswer(call -> withId(call.getArgument(0), 33L));

        service.createGroup(TEACHER, UNIT, CONTEXT, new GroupMembersRequest(List.of(1L, 2L, 3L, 4L, 5L)));

        ArgumentCaptor<StudentGroup> saved = ArgumentCaptor.forClass(StudentGroup.class);
        verify(groups).save(saved.capture());
        assertEquals(1, saved.getValue().getGenerationNumber());
        assertEquals(1, saved.getValue().getGroupNumber());
        verify(contextStudents).saveAll(org.mockito.ArgumentMatchers.argThat(list -> {
            List<GroupContextStudent> added = new ArrayList<>();
            list.forEach(added::add);
            return added.size() == 1 && added.get(0).getStudent().getId() == 5L;
        }));
        verify(members).saveAll(org.mockito.ArgumentMatchers.argThat(list -> {
            List<GroupMember> added = new ArrayList<>();
            list.forEach(added::add);
            return added.size() == 5;
        }));
    }

    @Test
    void editingAGroupOnlyRemovesWhoLeavesAndAddsWhoEnters() {
        givenGeneration(1);

        // Grupo 1 (alunos 1–5) passa a 1–4: o aluno 5 sai e mais ninguém muda.
        TeacherGroupsResponse response = service.updateGroup(
                TEACHER, UNIT, CONTEXT, 31L, new GroupMembersRequest(List.of(1L, 2L, 3L, 4L)));

        verify(members).deleteAll(org.mockito.ArgumentMatchers.argThat(list -> {
            List<GroupMember> removed = new ArrayList<>();
            list.forEach(removed::add);
            return removed.size() == 1 && removed.get(0).getStudent().getId() == 5L;
        }));
        verify(members).saveAll(List.of());
        verify(groups).saveAll(any());
        assertEquals(2, response.groups().size());
    }

    // ---------------------------------------------------------------------
    // Ajudas
    // ---------------------------------------------------------------------

    /**
     * Geração {@code number} com o grupo 1 (alunos 1–5, ORG 4 e PROG 2) e o grupo 2
     * (alunos 6–9, ORG 2 e PROG 5).
     */
    private void givenGeneration(int number) {
        StudentGroup g1 = group(31L, number, 1);
        StudentGroup g2 = group(32L, number, 2);
        when(groups.findByContext_IdOrderByGenerationNumberDescGroupNumberAsc(CONTEXT)).thenReturn(List.of(g1, g2));
        when(groups.findByContext_IdAndGenerationNumberOrderByGroupNumberAsc(CONTEXT, number))
                .thenReturn(List.of(g1, g2));
        when(groups.findById(31L)).thenReturn(Optional.of(g1));
        when(groups.findById(32L)).thenReturn(Optional.of(g2));
        List<GroupMember> first = members(g1, 1, 5, 4.0, 2.0);
        List<GroupMember> second = members(g2, 6, 9, 2.0, 5.0);
        when(members.findByGroup_IdOrderByStudent_StudentNumberAsc(31L)).thenReturn(first);
        when(members.findByGroup_IdOrderByStudent_StudentNumberAsc(32L)).thenReturn(second);
        List<GroupMember> all = new ArrayList<>(first);
        all.addAll(second);
        when(members.findByContextIdAndGenerationNumber(CONTEXT, number)).thenReturn(all);
    }

    /** Membros {@code from}..{@code to} de um grupo, todos com o mesmo perfil ORG/PROG. */
    private List<GroupMember> members(StudentGroup group, int from, int to, double org, double prog) {
        return LongStream.rangeClosed(from, to)
                .mapToObj(id -> {
                    when(profiles.getStudentProfile(id)).thenReturn(profile(org, prog));
                    return new GroupMember(group, students.get((int) id - 1), CONTEXT,
                            group.getGenerationNumber());
                })
                .toList();
    }

    private StudentGroup group(Long id, int generation, int number) {
        return withId(new StudentGroup(context, generation, number, 3.0, 0.0), id);
    }

    private static StudentProfile profile(double org, double prog) {
        return new StudentProfile(0, 1.0, List.of(level("ORG", org), level("PROG", prog)));
    }

    private static CompetencyLevel level(String code, double value) {
        return new CompetencyLevel(code, value, LevelOrigin.QUIZ, 1.0, value, null, List.of());
    }

    private static <T> T withId(T entity, Long id) {
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }
}

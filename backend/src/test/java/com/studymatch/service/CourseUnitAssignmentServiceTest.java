package com.studymatch.service;

import com.studymatch.dto.CreateTeacherRequest;
import com.studymatch.model.AppUser;
import com.studymatch.model.CourseUnit;
import com.studymatch.model.CourseUnitStudent;
import com.studymatch.model.CourseUnitTeacher;
import com.studymatch.model.Role;
import com.studymatch.model.Student;
import com.studymatch.repository.AppUserRepository;
import com.studymatch.repository.CourseUnitRepository;
import com.studymatch.repository.CourseUnitStudentRepository;
import com.studymatch.repository.CourseUnitTeacherRepository;
import com.studymatch.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes das atribuições do admin (R1 e R2), com Mockito.
 */
class CourseUnitAssignmentServiceTest {

    private CourseUnitRepository courseUnits;
    private StudentRepository students;
    private AppUserRepository users;
    private CourseUnitTeacherRepository teachers;
    private CourseUnitStudentRepository enrolled;
    private CourseUnitAssignmentService service;

    private final CourseUnit qs = withId(new CourseUnit("QS", "Qualidade de Software", 6, 3, 1), 10L);
    private final Student ana = withId(new Student("a1", "Ana", "ana@ulusofona.pt", "LEI", 3), 7L);
    private final AppUser teacher = withId(new AppUser("prof", "hash", Role.DOCENTE, "Prof. Rita", null), 3L);

    @BeforeEach
    void setUp() {
        courseUnits = mock(CourseUnitRepository.class);
        students = mock(StudentRepository.class);
        users = mock(AppUserRepository.class);
        teachers = mock(CourseUnitTeacherRepository.class);
        enrolled = mock(CourseUnitStudentRepository.class);
        service = new CourseUnitAssignmentService(courseUnits, students, users, teachers, enrolled,
                new BCryptPasswordEncoder(4));

        when(courseUnits.findById(10L)).thenReturn(Optional.of(qs));
        when(students.findById(7L)).thenReturn(Optional.of(ana));
        when(users.findById(3L)).thenReturn(Optional.of(teacher));
        when(teachers.findByCourseUnitIdOrderByTeacherDisplayNameAsc(10L)).thenReturn(List.of());
        when(enrolled.findByCourseUnitIdOrderByStudentNameAsc(10L)).thenReturn(List.of());
    }

    // --- R1 ---------------------------------------------------------------

    @Test
    void enrollsAStudentInTheCourseUnit() {
        service.enrollStudent(10L, 7L);

        ArgumentCaptor<CourseUnitStudent> saved = ArgumentCaptor.forClass(CourseUnitStudent.class);
        verify(enrolled).save(saved.capture());
        assertEquals(qs, saved.getValue().getCourseUnit());
        assertEquals(ana, saved.getValue().getStudent());
    }

    @Test
    void rejectsAStudentAlreadyEnrolledInTheSameCourseUnit() {
        when(enrolled.existsByCourseUnitIdAndStudentId(10L, 7L)).thenReturn(true);

        var error = assertThrows(ResponseStatusException.class, () -> service.enrollStudent(10L, 7L));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        assertEquals("Este aluno já está inscrito nesta UC.", error.getReason());
        verify(enrolled, never()).save(any());
    }

    @Test
    void returns404ForUnknownCourseUnitOrStudent() {
        when(courseUnits.findById(99L)).thenReturn(Optional.empty());
        when(students.findById(98L)).thenReturn(Optional.empty());

        assertEquals(HttpStatus.NOT_FOUND,
                assertThrows(ResponseStatusException.class, () -> service.enrollStudent(99L, 7L)).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND,
                assertThrows(ResponseStatusException.class, () -> service.enrollStudent(10L, 98L)).getStatusCode());
    }

    @Test
    void removesAnEnrolledStudentAndFailsIfNotEnrolled() {
        CourseUnitStudent link = new CourseUnitStudent(qs, ana);
        when(enrolled.findByCourseUnitIdAndStudentId(10L, 7L)).thenReturn(Optional.of(link));

        service.removeStudent(10L, 7L);
        verify(enrolled).delete(link);

        when(enrolled.findByCourseUnitIdAndStudentId(10L, 8L)).thenReturn(Optional.empty());
        assertEquals(HttpStatus.NOT_FOUND,
                assertThrows(ResponseStatusException.class, () -> service.removeStudent(10L, 8L)).getStatusCode());
    }

    // --- R2 ---------------------------------------------------------------

    @Test
    void assignsATeacherToTheCourseUnit() {
        service.assignTeacher(10L, 3L);

        ArgumentCaptor<CourseUnitTeacher> saved = ArgumentCaptor.forClass(CourseUnitTeacher.class);
        verify(teachers).save(saved.capture());
        assertEquals(teacher, saved.getValue().getTeacher());
    }

    @Test
    void onlyTeacherAccountsCanBeAssigned() {
        AppUser studentAccount = withId(new AppUser("a1", "hash", Role.ALUNO, "Ana", 7L), 4L);
        when(users.findById(4L)).thenReturn(Optional.of(studentAccount));

        var error = assertThrows(ResponseStatusException.class, () -> service.assignTeacher(10L, 4L));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verify(teachers, never()).save(any());
    }

    @Test
    void rejectsAssigningTheSameTeacherTwice() {
        when(teachers.existsByCourseUnitIdAndTeacherId(10L, 3L)).thenReturn(true);

        assertEquals(HttpStatus.CONFLICT,
                assertThrows(ResponseStatusException.class, () -> service.assignTeacher(10L, 3L)).getStatusCode());
    }

    // --- Contas de docente --------------------------------------------------

    @Test
    void createsATeacherAccountWithAHashedPassword() {
        when(users.save(any(AppUser.class))).thenAnswer(inv -> withId(inv.getArgument(0), 20L));

        var created = service.createTeacher(new CreateTeacherRequest(" rita ", " Prof. Rita ", "segredo123"));

        assertEquals("rita", created.username());
        assertEquals("Prof. Rita", created.displayName());
        ArgumentCaptor<AppUser> saved = ArgumentCaptor.forClass(AppUser.class);
        verify(users).save(saved.capture());
        assertEquals(Role.DOCENTE, saved.getValue().getRole());
        assertEquals(false, "segredo123".equals(saved.getValue().getPasswordHash()));
    }

    @Test
    void rejectsADuplicateUsernameOrAShortPassword() {
        when(users.existsByUsernameIgnoreCase("rita")).thenReturn(true);

        assertEquals(HttpStatus.CONFLICT, assertThrows(ResponseStatusException.class,
                () -> service.createTeacher(new CreateTeacherRequest("rita", "Rita", "segredo123"))).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
                () -> service.createTeacher(new CreateTeacherRequest("rui", "Rui", "curta"))).getStatusCode());
    }

    private static <T> T withId(T entity, Long id) {
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }
}

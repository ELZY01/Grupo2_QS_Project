package com.studymatch.service;

import com.studymatch.model.AppUser;
import com.studymatch.model.CourseUnit;
import com.studymatch.model.CourseUnitStudent;
import com.studymatch.model.CourseUnitTeacher;
import com.studymatch.model.Role;
import com.studymatch.model.Student;
import com.studymatch.repository.CourseUnitRepository;
import com.studymatch.repository.CourseUnitStudentRepository;
import com.studymatch.repository.CourseUnitTeacherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes da área do docente (R3, R7), com Mockito.
 */
class TeacherServiceTest {

    private CourseUnitRepository courseUnits;
    private CourseUnitTeacherRepository teachers;
    private CourseUnitStudentRepository enrolled;
    private TeacherService service;

    private final CourseUnit qs = withId(new CourseUnit("QS", "Qualidade de Software", 6, 3, 1), 10L);
    private final AppUser rita = withId(new AppUser("rita", "hash", Role.DOCENTE, "Prof. Rita", null), 3L);
    private final Student ana = withId(new Student("a1", "Ana", "ana@ulusofona.pt", "LEI", 3), 7L);

    @BeforeEach
    void setUp() {
        courseUnits = mock(CourseUnitRepository.class);
        teachers = mock(CourseUnitTeacherRepository.class);
        enrolled = mock(CourseUnitStudentRepository.class);
        service = new TeacherService(courseUnits, teachers, enrolled);
        when(courseUnits.existsById(10L)).thenReturn(true);
    }

    @Test
    void listsOnlyTheUnitsAssignedToTheTeacherWithTheStudentCount() {
        when(teachers.findByTeacherIdOrderByCourseUnitNameAsc(3L)).thenReturn(List.of(new CourseUnitTeacher(qs, rita)));
        when(enrolled.countByCourseUnitId(10L)).thenReturn(24L);

        var units = service.myCourseUnits(3L);

        assertEquals(1, units.size());
        assertEquals("QS", units.get(0).courseUnit().code());
        assertEquals(24L, units.get(0).studentCount());
    }

    @Test
    void listsTheStudentsOfAnAssignedUnit() {
        when(teachers.existsByCourseUnitIdAndTeacherId(10L, 3L)).thenReturn(true);
        when(enrolled.findByCourseUnitIdOrderByStudentNameAsc(10L)).thenReturn(List.of(new CourseUnitStudent(qs, ana)));

        var students = service.studentsOf(3L, 10L);

        assertEquals(List.of("Ana"), students.stream().map(s -> s.name()).toList());
    }

    @Test
    void forbidsTheStudentsOfAUnitFromAnotherTeacher() {
        when(teachers.existsByCourseUnitIdAndTeacherId(10L, 4L)).thenReturn(false);

        var error = assertThrows(ResponseStatusException.class, () -> service.studentsOf(4L, 10L));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
        verify(enrolled, never()).findByCourseUnitIdOrderByStudentNameAsc(10L);
    }

    @Test
    void returns404ForAnUnknownUnit() {
        when(courseUnits.existsById(99L)).thenReturn(false);

        assertEquals(HttpStatus.NOT_FOUND,
                assertThrows(ResponseStatusException.class, () -> service.studentsOf(3L, 99L)).getStatusCode());
    }

    private static <T> T withId(T entity, Long id) {
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }
}

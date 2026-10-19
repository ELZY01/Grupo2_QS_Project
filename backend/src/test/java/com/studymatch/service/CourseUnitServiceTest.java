package com.studymatch.service;

import com.studymatch.dto.CourseUnitResponse;
import com.studymatch.model.CourseUnit;
import com.studymatch.repository.CourseUnitRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Testes unitários do serviço de unidades curriculares.
 */
@ExtendWith(MockitoExtension.class)
class CourseUnitServiceTest {

    @Mock
    private CourseUnitRepository courseUnitRepository;

    @InjectMocks
    private CourseUnitService courseUnitService;

    @Test
    void returnsCourseUnitsOrderedByYearSemesterAndCode() {
        when(courseUnitRepository.findAll()).thenReturn(List.of(
                new CourseUnit("ES", "Engenharia de Software", 6, 2, 2),
                new CourseUnit("POO", "Programação Orientada a Objetos", 6, 1, 2),
                new CourseUnit("BD", "Bases de Dados", 6, 2, 1),
                new CourseUnit("PROG1", "Programação I", 6, 1, 1)
        ));

        List<CourseUnitResponse> result = courseUnitService.getAllCourseUnits();

        assertEquals(
                List.of("PROG1", "POO", "BD", "ES"),
                result.stream().map(CourseUnitResponse::code).toList()
        );
        assertEquals("Programação I", result.get(0).name());
        assertEquals(6, result.get(0).ects());
    }

    @Test
    void returnsEmptyListWhenThereAreNoCourseUnits() {
        when(courseUnitRepository.findAll()).thenReturn(List.of());

        assertTrue(courseUnitService.getAllCourseUnits().isEmpty());
    }
}

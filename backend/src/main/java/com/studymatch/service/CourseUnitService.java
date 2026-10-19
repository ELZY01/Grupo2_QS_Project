package com.studymatch.service;

import com.studymatch.dto.CourseUnitResponse;
import com.studymatch.model.CourseUnit;
import com.studymatch.repository.CourseUnitRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/**
 * Consulta das unidades curriculares disponíveis.
 */
@Service
public class CourseUnitService {

    private final CourseUnitRepository courseUnitRepository;

    public CourseUnitService(CourseUnitRepository courseUnitRepository) {
        this.courseUnitRepository = courseUnitRepository;
    }

    /**
     * Devolve todas as UCs, ordenadas por ano curricular, semestre e código,
     * para aparecerem pela ordem do plano de estudos.
     */
    @Transactional(readOnly = true)
    public List<CourseUnitResponse> getAllCourseUnits() {
        return courseUnitRepository.findAll()
                .stream()
                .sorted(Comparator
                        .comparing(CourseUnit::getAcademicYear)
                        .thenComparing(CourseUnit::getSemester)
                        .thenComparing(CourseUnit::getCode))
                .map(CourseUnitResponse::from)
                .toList();
    }
}

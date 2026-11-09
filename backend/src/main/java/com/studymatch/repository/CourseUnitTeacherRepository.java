package com.studymatch.repository;

import com.studymatch.model.CourseUnitTeacher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Docentes associados às unidades curriculares.
 */
public interface CourseUnitTeacherRepository extends JpaRepository<CourseUnitTeacher, Long> {

    boolean existsByCourseUnitIdAndTeacherId(Long courseUnitId, Long teacherId);

    Optional<CourseUnitTeacher> findByCourseUnitIdAndTeacherId(Long courseUnitId, Long teacherId);

    List<CourseUnitTeacher> findByCourseUnitIdOrderByTeacherDisplayNameAsc(Long courseUnitId);

    List<CourseUnitTeacher> findByTeacherIdOrderByCourseUnitNameAsc(Long teacherId);
}

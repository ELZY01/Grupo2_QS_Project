package com.studymatch.repository;

import com.studymatch.model.CourseUnitStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Alunos inscritos nas unidades curriculares que estão a frequentar.
 */
public interface CourseUnitStudentRepository extends JpaRepository<CourseUnitStudent, Long> {

    boolean existsByCourseUnitIdAndStudentId(Long courseUnitId, Long studentId);

    Optional<CourseUnitStudent> findByCourseUnitIdAndStudentId(Long courseUnitId, Long studentId);

    List<CourseUnitStudent> findByCourseUnitIdOrderByStudentNameAsc(Long courseUnitId);

    long countByCourseUnitId(Long courseUnitId);

    /**
     * O docente leciona alguma UC onde o aluno está inscrito? (R3)
     */
    @Query("""
            select count(cs) > 0
            from CourseUnitStudent cs, CourseUnitTeacher ct
            where cs.courseUnit = ct.courseUnit
              and ct.teacher.id = :teacherId
              and cs.student.id = :studentId
            """)
    boolean teacherHasStudent(@Param("teacherId") Long teacherId, @Param("studentId") Long studentId);
}

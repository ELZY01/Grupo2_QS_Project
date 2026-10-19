package com.studymatch.repository;

import com.studymatch.model.CourseUnitCompetency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CourseUnitCompetencyRepository extends JpaRepository<CourseUnitCompetency, Long> {

    // Todos os pesos UC → competência, já com a UC e a competência carregadas
    // (evita uma consulta extra por cada associação).
    @Query("SELECT cuc FROM CourseUnitCompetency cuc JOIN FETCH cuc.courseUnit JOIN FETCH cuc.competency")
    List<CourseUnitCompetency> findAllWithCourseUnitAndCompetency();
}

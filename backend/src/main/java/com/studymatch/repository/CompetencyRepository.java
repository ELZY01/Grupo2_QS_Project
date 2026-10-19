package com.studymatch.repository;

import com.studymatch.model.Competency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompetencyRepository extends JpaRepository<Competency, Long> {

    // Todas as competências, por ordem de criação (ORG, TEMPO, PROG, EQUIPA).
    List<Competency> findAllByOrderByIdAsc();
}

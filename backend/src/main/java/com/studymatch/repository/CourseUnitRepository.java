
package com.studymatch.repository;

import com.studymatch.model.CourseUnit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Permite consultar as unidades curriculares
 * existentes na base de dados.
 *
 * O método findById já é fornecido pelo JpaRepository.
 */
public interface CourseUnitRepository
        extends JpaRepository<CourseUnit, Long> {

    // Usado pelo DataSeeder para encontrar a UC de teste.
    Optional<CourseUnit> findByCode(String code);
}

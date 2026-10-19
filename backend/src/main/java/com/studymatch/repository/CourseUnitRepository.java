
package com.studymatch.repository;

import com.studymatch.model.CourseUnit;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Permite consultar as unidades curriculares
 * existentes na base de dados.
 *
 * O método findById já é fornecido pelo JpaRepository.
 */
public interface CourseUnitRepository
        extends JpaRepository<CourseUnit, Long> {
}

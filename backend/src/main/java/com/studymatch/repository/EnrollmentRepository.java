
package com.studymatch.repository;

import com.studymatch.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositório responsável pelo acesso às inscrições
 * guardadas na base de dados.
 *
 * O Spring Data JPA implementa automaticamente
 * os métodos declarados nesta interface.
 */
public interface EnrollmentRepository
        extends JpaRepository<Enrollment, Long> {

    /**
     * Obtém o histórico de um estudante,
     * ordenado pela unidade curricular e pela tentativa.
     */
    List<Enrollment> findByStudentIdOrderByCourseUnitIdAscAttemptNumberAsc(
            Long studentId
    );

    /**
     * Obtém as tentativas de um estudante
     * numa determinada unidade curricular.
     *
     * Permite calcular a próxima tentativa.
     */
    List<Enrollment> findByStudentIdAndCourseUnitIdOrderByAttemptNumberDesc(
            Long studentId,
            Long courseUnitId
    );
}

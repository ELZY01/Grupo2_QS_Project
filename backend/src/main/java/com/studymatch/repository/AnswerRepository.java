package com.studymatch.repository;

import com.studymatch.model.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositório JPA responsável pela gestão e persistência das respostas individuais (Answer).
 */
@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {

    /**
     * Procura todas as respostas associadas a uma submissão específica.
     *
     * @param submissionId ID da submissão.
     * @return Lista de respostas da submissão.
     */
    List<Answer> findBySubmissionId(Long submissionId);
}
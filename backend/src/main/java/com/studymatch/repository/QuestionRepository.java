package com.studymatch.repository;

import com.studymatch.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositório responsável pela persistência e operações de base de dados
 * relacionadas com a entidade Question (Perguntas do Quiz).
 */
@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    /**
     * Procura e devolve todas as perguntas associadas a um quiz específico,
     * ordenadas de forma ascendente com base na propriedade questionOrder.
     *
     * @param quizId O ID do quiz cujas perguntas se pretende obter.
     * @return Uma lista de perguntas ordenadas do quiz especificado.
     */
    List<Question> findByQuizIdOrderByQuestionOrderAsc(Long quizId);
}

package com.studymatch.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Representa a resposta de um estudante
 * a uma pergunta do quiz de autoavaliação.
 *
 * Cada resposta:
 * - Pertence a uma submissão do quiz.
 * - Está associada a uma pergunta.
 * - Possui um valor inteiro entre 1 e 5.
 *
 * Uma pergunta só pode ter uma resposta
 * dentro da mesma submissão.
 */
@Entity
@Table(
        name = "answers",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_answer_submission_question",
                        columnNames = {"submission_id", "question_id"}
                )
        }
)
public class Answer {

    // Identificador único da resposta.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Submissão à qual a resposta pertence.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private QuizSubmission submission;

    // Pergunta que está a ser respondida.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    // Valor da resposta, entre 1 e 5.
    @Column(nullable = false)
    private Integer value;

    /**
     * Construtor vazio obrigatório para o Hibernate.
     */
    public Answer() {
    }

    /**
     * Construtor utilizado para criar uma resposta.
     */
    public Answer(
            QuizSubmission submission,
            Question question,
            Integer value) {

        this.submission = submission;
        this.question = question;
        setValue(value);
    }

    public Long getId() {
        return id;
    }

    public QuizSubmission getSubmission() {
        return submission;
    }

    public void setSubmission(QuizSubmission submission) {
        this.submission = submission;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public Integer getValue() {
        return value;
    }

    /**
     * Valida o valor da resposta.
     *
     * Só são permitidos valores inteiros
     * entre 1 e 5, inclusive.
     */
    public void setValue(Integer value) {
        if (value == null || value < 1 || value > 5) {
            throw new IllegalArgumentException(
                    "A resposta deve ter um valor entre 1 e 5."
            );
        }

        this.value = value;
    }
}

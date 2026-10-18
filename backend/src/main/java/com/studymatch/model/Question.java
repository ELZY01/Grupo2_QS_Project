package com.studymatch.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
 * Representa uma pergunta de um quiz do StudyMatch.
 *
 * Cada pergunta:
 * - Pertence a um quiz.
 * - Avalia uma competência.
 * - Possui uma posição no questionário.
 */
@Entity
@Table(
        name = "questions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_question_quiz_order",
                        columnNames = {"quiz_id", "question_order"}
                )
        }
)
public class Question {

    // Identificador único da pergunta.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Quiz ao qual a pergunta pertence.
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    // Competência avaliada pela pergunta.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "competency_id", nullable = false)
    private Competency competency;

    // Texto apresentado ao estudante.
    @Column(nullable = false, length = 1000)
    private String text;

    // Posição da pergunta no quiz: 1, 2, 3...
    @Column(name = "question_order", nullable = false)
    private Integer questionOrder;

    /**
     * Construtor vazio obrigatório para o Hibernate.
     */
    public Question() {
    }

    /**
     * Construtor utilizado para criar uma pergunta.
     */
    public Question(
            Quiz quiz,
            Competency competency,
            String text,
            Integer questionOrder) {

        this.quiz = quiz;
        this.competency = competency;
        this.text = text;
        this.questionOrder = questionOrder;
    }

    public Long getId() {
        return id;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    public Competency getCompetency() {
        return competency;
    }

    public void setCompetency(Competency competency) {
        this.competency = competency;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getQuestionOrder() {
        return questionOrder;
    }

    public void setQuestionOrder(Integer questionOrder) {
        this.questionOrder = questionOrder;
    }
}
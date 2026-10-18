
package com.studymatch.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Representa uma submissão do quiz de autoavaliação.
 *
 * Cada submissão pertence a um estudante e a um quiz.
 *
 * Um estudante pode responder ao mesmo quiz várias vezes.
 * Todas as submissões ficam guardadas para preservar
 * o histórico de autoavaliação.
 */
@Entity
@Table(name = "quiz_submissions")
public class QuizSubmission {

    // Identificador único da submissão.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Estudante que respondeu ao questionário.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    // Quiz ao qual o estudante respondeu.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    // Data e hora em que a submissão foi criada.
    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    /**
     * Construtor vazio obrigatório para o Hibernate.
     */
    public QuizSubmission() {
    }

    /**
     * Construtor para criar uma submissão.
     *
     * A data será preenchida automaticamente
     * antes de guardar a submissão.
     */
    public QuizSubmission(Student student, Quiz quiz) {
        this.student = student;
        this.quiz = quiz;
    }

    /**
     * Executado automaticamente pelo Hibernate
     * antes de inserir a submissão na base de dados.
     *
     * Preenche a data apenas se ainda não existir.
     */
    @PrePersist
    public void preencherDataSubmissao() {
        if (submittedAt == null) {
            submittedAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
}


package com.studymatch.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Representa um questionário de autoavaliação
 * utilizado no arranque a frio do StudyMatch.
 *
 * Cada quiz terá 20 perguntas:
 * 5 perguntas por cada uma das 4 competências base.
 *
 * As perguntas serão associadas a este quiz
 * através da entidade Question.
 */
@Entity
@Table(name = "quizzes")
public class Quiz {

    // Identificador único do quiz.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Versão do questionário.
    // Permite distinguir versões diferentes do quiz.
    @Column(nullable = false, unique = true)
    private String version;

    /**
     * Construtor vazio obrigatório para o Hibernate.
     */
    public Quiz() {
    }

    /**
     * Construtor para criar uma versão do quiz.
     *
     * Exemplo: new Quiz("1.0");
     */
    public Quiz(String version) {
        this.version = version;
    }

    // Devolve o identificador do quiz.
    public Long getId() {
        return id;
    }

    // Devolve a versão do questionário.
    public String getVersion() {
        return version;
    }

    // Atualiza a versão do questionário.
    public void setVersion(String version) {
        this.version = version;
    }
}


package com.studymatch.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Representa uma competência académica do StudyMatch.
 *
 * Uma competência pode ser desenvolvida através
 * das unidades curriculares e avaliada no perfil
 * académico do estudante.
 *
 * Existem quatro competências base utilizadas
 * no quiz inicial de autoavaliação.
 */
@Entity
@Table(name = "competencies")
public class Competency {

    // Identificador único da competência.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Código único da competência.
    @Column(nullable = false, unique = true)
    private String code;

    // Nome da competência.
    @Column(nullable = false)
    private String name;

    // Descrição da competência.
    @Column
    private String description;

    // Indica se pertence às quatro competências base.
    // true = competência base do quiz.
    // false = competência adicional.
    @Column(name = "is_base", nullable = false)
    private boolean base;

    /**
     * Construtor vazio obrigatório para o Hibernate.
     */
    public Competency() {
    }

    /**
     * Construtor para criar uma competência.
     *
     * O ID é gerado automaticamente pela base de dados.
     */
    public Competency(String code, String name,
                      String description, boolean base) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.base = base;
    }

    // Devolve o identificador da competência.
    public Long getId() {
        return id;
    }

    // Devolve o código da competência.
    public String getCode() {
        return code;
    }

    // Atualiza o código da competência.
    public void setCode(String code) {
        this.code = code;
    }

    // Devolve o nome da competência.
    public String getName() {
        return name;
    }

    // Atualiza o nome da competência.
    public void setName(String name) {
        this.name = name;
    }

    // Devolve a descrição da competência.
    public String getDescription() {
        return description;
    }

    // Atualiza a descrição da competência.
    public void setDescription(String description) {
        this.description = description;
    }

    // Indica se a competência é uma das quatro competências base.
    public boolean isBase() {
        return base;
    }

    // Define se a competência pertence ao conjunto base.
    public void setBase(boolean base) {
        this.base = base;
    }
}

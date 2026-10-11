package com.studymatch.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Representa a tabela persistencia_teste da base de dados.
// O Hibernate utiliza esta classe para converter objetos Java em registos SQL.
@Entity
@Table(name = "persistencia_teste")
public class PersistenciaTeste {

    // Identificador único gerado automaticamente pelo PostgreSQL.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Informação que vamos guardar para testar a persistência.
    private String descricao;

    // Construtor vazio exigido pelo JPA/Hibernate.
    public PersistenciaTeste() {
    }

    // Construtor utilizado para criar um novo registo.
    public PersistenciaTeste(String descricao) {
        this.descricao = descricao;
    }

    public Long getId() {
        return id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
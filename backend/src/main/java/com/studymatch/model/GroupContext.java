
package com.studymatch.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Representa um contexto de formacao de grupos.
 *
 * Um contexto guarda os estudantes selecionados
 * e permite gerar diferentes versoes de grupos.
 */
@Entity
@Table(name = "group_contexts")
public class GroupContext {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    /**
     * UC a que o contexto pertence (Issue #106, R4). Fica vazio nos contextos
     * criados pelo admin em /api/group-contexts.
     */
    @Column(name = "course_unit_id")
    private Long courseUnitId;

    public GroupContext() {
    }

    public GroupContext(String name) {
        this.name = name;
    }

    public GroupContext(String name, Long courseUnitId) {
        this.name = name;
        this.courseUnitId = courseUnitId;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public Long getCourseUnitId() {
        return courseUnitId;
    }
}

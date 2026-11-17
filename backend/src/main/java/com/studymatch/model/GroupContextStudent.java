
package com.studymatch.model;

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
 * Representa um estudante associado a um contexto de formacao.
 *
 * Cada estudante so pode ser associado uma vez
 * ao mesmo contexto.
 */
@Entity
@Table(
        name = "group_context_students",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_gcs_context_student",
                columnNames = {"context_id", "student_id"}
        )
)
public class GroupContextStudent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "context_id", nullable = false)
    private GroupContext context;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    public GroupContextStudent() {
    }

    public GroupContextStudent(GroupContext context, Student student) {
        this.context = context;
        this.student = student;
    }

    public Long getId() {
        return id;
    }

    public GroupContext getContext() {
        return context;
    }

    public Student getStudent() {
        return student;
    }
}


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
 * Representa um estudante pertencente a um grupo gerado.
 *
 * Um estudante so pode pertencer a um grupo por geracao.
 */
@Entity
@Table(
        name = "group_members",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_gm_group_student",
                        columnNames = {"group_id", "student_id"}
                ),
                @UniqueConstraint(
                        name = "uq_gm_student_generation",
                        columnNames = {
                                "context_id",
                                "generation_number",
                                "student_id"
                        }
                )
        }
)
public class GroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private StudentGroup group;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "context_id", nullable = false)
    private Long contextId;

    @Column(name = "generation_number", nullable = false)
    private Integer generationNumber;

    public GroupMember() {
    }

    public GroupMember(
            StudentGroup group,
            Student student,
            Long contextId,
            Integer generationNumber) {

        this.group = group;
        this.student = student;
        this.contextId = contextId;
        this.generationNumber = generationNumber;
    }

    public Long getId() {
        return id;
    }

    public StudentGroup getGroup() {
        return group;
    }

    public Student getStudent() {
        return student;
    }

    public Long getContextId() {
        return contextId;
    }

    public Integer getGenerationNumber() {
        return generationNumber;
    }
}

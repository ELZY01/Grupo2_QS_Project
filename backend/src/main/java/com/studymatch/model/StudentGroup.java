
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
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

/**
 * Representa um grupo criado durante uma geracao.
 *
 * Um contexto pode ter varias geracoes, preservando
 * os grupos das geracoes anteriores.
 */
@Entity
@Table(
        name = "student_groups",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_sg_generation_group",
                        columnNames = {
                                "context_id",
                                "generation_number",
                                "group_number"
                        }
                ),
                @UniqueConstraint(
                        name = "uq_sg_context_generation_id",
                        columnNames = {
                                "context_id",
                                "generation_number",
                                "id"
                        }
                )
        }
)
public class StudentGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "context_id", nullable = false)
    private GroupContext context;

    @Column(name = "generation_number", nullable = false)
    private Integer generationNumber;

    @Column(name = "group_number", nullable = false)
    private Integer groupNumber;

    @Column(name = "average")
    private Double average;

    @Column(name = "gap_to_others")
    private Double gapToOthers;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    public StudentGroup() {
    }

    public StudentGroup(
            GroupContext context,
            Integer generationNumber,
            Integer groupNumber,
            Double average,
            Double gapToOthers) {

        this.context = context;
        this.generationNumber = generationNumber;
        this.groupNumber = groupNumber;
        this.average = average;
        this.gapToOthers = gapToOthers;
    }

    @PrePersist
    protected void onCreate() {
        if (generatedAt == null) {
            generatedAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public GroupContext getContext() {
        return context;
    }

    public Integer getGenerationNumber() {
        return generationNumber;
    }

    public Integer getGroupNumber() {
        return groupNumber;
    }

    public Double getAverage() {
        return average;
    }

    public Double getGapToOthers() {
        return gapToOthers;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }
}

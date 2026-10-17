
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
 * Representa a relação entre uma unidade curricular
 * e uma competência.
 *
 * Cada associação tem um peso entre 0 (exclusivo)
 * e 1 (inclusivo).
 *
 * Exemplo: 0.40 significa que a UC contribui
 * com um peso de 40% para essa competência.
 */
@Entity
@Table(
        name = "course_unit_competencies",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_course_unit_competency",
                        columnNames = {"course_unit_id", "competency_id"}
                )
        }
)
public class CourseUnitCompetency {

    // Identificador único da associação.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Unidade curricular associada.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_unit_id", nullable = false)
    private CourseUnit courseUnit;

    // Competência associada.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competency_id", nullable = false)
    private Competency competency;

    // Peso da associação: maior que 0 e menor ou igual a 1.
    @Column(nullable = false)
    private Double weight;

    /**
     * Construtor vazio obrigatório para o Hibernate.
     */
    public CourseUnitCompetency() {
    }

    /**
     * Construtor para criar uma associação.
     */
    public CourseUnitCompetency(
            CourseUnit courseUnit,
            Competency competency,
            Double weight) {

        this.courseUnit = courseUnit;
        this.competency = competency;
        setWeight(weight);
    }

    public Long getId() {
        return id;
    }

    public CourseUnit getCourseUnit() {
        return courseUnit;
    }

    public void setCourseUnit(CourseUnit courseUnit) {
        this.courseUnit = courseUnit;
    }

    public Competency getCompetency() {
        return competency;
    }

    public void setCompetency(Competency competency) {
        this.competency = competency;
    }

    public Double getWeight() {
        return weight;
    }

    /**
     * Atualiza o peso apenas se estiver no intervalo ]0, 1].
     *
     * Valores como 0, -0.5 ou 1.5 são inválidos.
     */
    public void setWeight(Double weight) {
        if (weight == null
                || !Double.isFinite(weight)
                || weight <= 0
                || weight > 1) {
            throw new IllegalArgumentException(
                    "O peso deve ser maior que 0 e menor ou igual a 1."
            );
        }

        this.weight = weight;
    }
}


package com.studymatch.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Representa uma inscrição de um estudante
 * numa unidade curricular.
 *
 * Permite guardar:
 * - O estudante inscrito;
 * - A unidade curricular;
 * - O ano letivo e o semestre;
 * - O número da tentativa;
 * - A nota obtida;
 * - O estado da inscrição.
 *
 * O histórico de tentativas é preservado.
 */
@Entity
@Table(
        name = "enrollments",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_enrollment_attempt",
                        columnNames = {
                                "student_id",
                                "course_unit_id",
                                "attempt_number"
                        }
                )
        }
)
public class Enrollment {

    // Identificador único da inscrição.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Estudante ao qual pertence a inscrição.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    // Unidade curricular em que o estudante se inscreveu.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_unit_id", nullable = false)
    private CourseUnit courseUnit;

    // Ano de início do ano letivo.
    // Exemplo: 2026 representa o ano letivo 2026/2027.
    @Column(name = "school_year", nullable = false)
    private Integer schoolYear;

    // Semestre em que ocorreu a inscrição.
    @Column(nullable = false)
    private Integer semester;

    // Número sequencial da tentativa: 1, 2, 3...
    @Column(name = "attempt_number", nullable = false)
    private Integer attemptNumber;

    // Nota entre 0 e 20.
    // Pode ser null quando a inscrição está em curso.
    @Column
    private Integer grade;

    // Estado atual da inscrição.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnrollmentStatus status;

    /**
     * Estados possíveis de uma inscrição.
     */
    public enum EnrollmentStatus {
        EM_CURSO,
        APROVADO,
        REPROVADO
    }

    /**
     * Construtor vazio obrigatório para o Hibernate.
     */
    public Enrollment() {
    }

    /**
     * Construtor para criar uma inscrição.
     *
     * O ID não é recebido porque é gerado
     * automaticamente pela base de dados.
     */
    public Enrollment(
            Student student,
            CourseUnit courseUnit,
            Integer schoolYear,
            Integer semester,
            Integer attemptNumber,
            Integer grade,
            EnrollmentStatus status) {

        this.student = student;
        this.courseUnit = courseUnit;
        this.schoolYear = schoolYear;
        this.semester = semester;
        this.attemptNumber = attemptNumber;
        this.grade = grade;
        this.status = status;
    }

    // Devolve o identificador da inscrição.
    public Long getId() {
        return id;
    }

    // Devolve o estudante associado.
    public Student getStudent() {
        return student;
    }

    // Atualiza o estudante associado.
    public void setStudent(Student student) {
        this.student = student;
    }

    // Devolve a unidade curricular.
    public CourseUnit getCourseUnit() {
        return courseUnit;
    }

    // Atualiza a unidade curricular.
    public void setCourseUnit(CourseUnit courseUnit) {
        this.courseUnit = courseUnit;
    }

    // Devolve o ano de início do ano letivo.
    public Integer getSchoolYear() {
        return schoolYear;
    }

    // Atualiza o ano letivo.
    public void setSchoolYear(Integer schoolYear) {
        this.schoolYear = schoolYear;
    }

    // Devolve o semestre.
    public Integer getSemester() {
        return semester;
    }

    // Atualiza o semestre.
    public void setSemester(Integer semester) {
        this.semester = semester;
    }

    // Devolve o número da tentativa.
    public Integer getAttemptNumber() {
        return attemptNumber;
    }

    // Atualiza o número da tentativa.
    public void setAttemptNumber(Integer attemptNumber) {
        this.attemptNumber = attemptNumber;
    }

    // Devolve a nota.
    public Integer getGrade() {
        return grade;
    }

    // Atualiza a nota.
    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    // Devolve o estado da inscrição.
    public EnrollmentStatus getStatus() {
        return status;
    }

    // Atualiza o estado da inscrição.
    public void setStatus(EnrollmentStatus status) {
        this.status = status;
    }
}

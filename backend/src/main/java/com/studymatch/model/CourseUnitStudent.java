package com.studymatch.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Aluno inscrito numa unidade curricular que está a frequentar (R1).
 *
 * É diferente do histórico académico (Enrollment, com notas e tentativas):
 * aqui só se guarda que o aluno pertence à turma desta UC. Só o admin cria e remove.
 */
@Entity
@Table(name = "course_unit_students",
        uniqueConstraints = @UniqueConstraint(columnNames = {"course_unit_id", "student_id"}))
public class CourseUnitStudent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "course_unit_id", nullable = false)
    private CourseUnit courseUnit;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    public CourseUnitStudent() {
    }

    public CourseUnitStudent(CourseUnit courseUnit, Student student) {
        this.courseUnit = courseUnit;
        this.student = student;
    }

    public Long getId() {
        return id;
    }

    public CourseUnit getCourseUnit() {
        return courseUnit;
    }

    public Student getStudent() {
        return student;
    }
}

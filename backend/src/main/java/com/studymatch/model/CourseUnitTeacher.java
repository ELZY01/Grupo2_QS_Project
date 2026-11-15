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
 * Docente associado a uma unidade curricular (R2). Só o admin cria e remove.
 */
@Entity
@Table(name = "course_unit_teachers",
        uniqueConstraints = @UniqueConstraint(columnNames = {"course_unit_id", "teacher_id"}))
public class CourseUnitTeacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "course_unit_id", nullable = false)
    private CourseUnit courseUnit;

    // Conta com role DOCENTE.
    @ManyToOne(optional = false)
    @JoinColumn(name = "teacher_id", nullable = false)
    private AppUser teacher;

    public CourseUnitTeacher() {
    }

    public CourseUnitTeacher(CourseUnit courseUnit, AppUser teacher) {
        this.courseUnit = courseUnit;
        this.teacher = teacher;
    }

    public Long getId() {
        return id;
    }

    public CourseUnit getCourseUnit() {
        return courseUnit;
    }

    public AppUser getTeacher() {
        return teacher;
    }
}


package com.studymatch.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Representa um estudante registado no StudyMatch.
 *
 * Cada estudante tem um número institucional e
 * um email únicos, além dos seus dados académicos.
 */
@Entity
@Table(name = "students")
public class Student {

    // Identificador interno gerado pela base de dados.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Número institucional único do estudante.
    @Column(name = "student_number", nullable = false, unique = true)
    private String studentNumber;

    // Nome completo do estudante.
    @Column(nullable = false)
    private String name;

    // Email institucional único.
    @Column(nullable = false, unique = true)
    private String email;

    // Curso frequentado pelo estudante.
    // Nesta fase, guardamos o nome do curso diretamente.
    @Column(nullable = false)
    private String course;

    // Ano curricular atual do estudante.
    @Column(name = "academic_year", nullable = false)
    private Integer academicYear;

    /**
     * Construtor vazio obrigatório para o Hibernate.
     */
    public Student() {
    }

    /**
     * Construtor para criar um estudante.
     * O ID é gerado automaticamente pela base de dados.
     */
    public Student(String studentNumber, String name,
                   String email, String course,
                   Integer academicYear) {
        this.studentNumber = studentNumber;
        this.name = name;
        this.email = email;
        this.course = course;
        this.academicYear = academicYear;
    }

    public Long getId() {
        return id;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public void setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public Integer getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(Integer academicYear) {
        this.academicYear = academicYear;
    }
}

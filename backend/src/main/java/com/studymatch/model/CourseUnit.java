
package com.studymatch.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Representa uma unidade curricular do StudyMatch.
 *
 * Cada unidade curricular possui um código único,
 * um nome, créditos ECTS, ano curricular e semestre.
 *
 * Uma unidade curricular poderá ter várias inscrições
 * e estar associada a diferentes competências.
 */
@Entity
@Table(name = "course_units")
public class CourseUnit {

    // Identificador único gerado pela base de dados.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Código único da unidade curricular.
    @Column(nullable = false, unique = true)
    private String code;

    // Nome da unidade curricular.
    @Column(nullable = false)
    private String name;

    // Número de créditos ECTS da unidade curricular.
    @Column(nullable = false)
    private Integer ects;

    // Ano curricular em que a UC é lecionada.
    @Column(name = "academic_year", nullable = false)
    private Integer academicYear;

    // Semestre em que a UC é lecionada.
    @Column(nullable = false)
    private Integer semester;

    /**
     * Construtor vazio obrigatório para o Hibernate.
     */
    public CourseUnit() {
    }

    /**
     * Construtor utilizado para criar uma unidade curricular.
     *
     * O ID não é recebido porque será gerado
     * automaticamente pela base de dados.
     */
    public CourseUnit(String code, String name, Integer ects,
                      Integer academicYear, Integer semester) {
        this.code = code;
        this.name = name;
        this.ects = ects;
        this.academicYear = academicYear;
        this.semester = semester;
    }

    // Devolve o identificador da unidade curricular.
    public Long getId() {
        return id;
    }

    // Devolve o código da unidade curricular.
    public String getCode() {
        return code;
    }

    // Atualiza o código da unidade curricular.
    public void setCode(String code) {
        this.code = code;
    }

    // Devolve o nome da unidade curricular.
    public String getName() {
        return name;
    }

    // Atualiza o nome da unidade curricular.
    public void setName(String name) {
        this.name = name;
    }

    // Devolve os créditos ECTS.
    public Integer getEcts() {
        return ects;
    }

    // Atualiza os créditos ECTS.
    public void setEcts(Integer ects) {
        this.ects = ects;
    }

    // Devolve o ano curricular.
    public Integer getAcademicYear() {
        return academicYear;
    }

    // Atualiza o ano curricular.
    public void setAcademicYear(Integer academicYear) {
        this.academicYear = academicYear;
    }

    // Devolve o semestre.
    public Integer getSemester() {
        return semester;
    }

    // Atualiza o semestre.
    public void setSemester(Integer semester) {
        this.semester = semester;
    }
}

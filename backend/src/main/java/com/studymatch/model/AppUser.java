package com.studymatch.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Conta de acesso ao StudyMatch.
 *
 * A palavra-passe nunca é guardada em claro, só o hash BCrypt.
 * As contas de aluno estão ligadas ao respetivo estudante (studentId).
 */
@Entity
@Table(name = "app_users")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nome de utilizador único (no aluno é o número de estudante).
    @Column(nullable = false, unique = true)
    private String username;

    // Hash BCrypt da palavra-passe.
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // Nome a mostrar na interface.
    @Column(name = "display_name", nullable = false)
    private String displayName;

    // Estudante associado (só nas contas de aluno).
    @Column(name = "student_id", unique = true)
    private Long studentId;

    /**
     * Construtor vazio obrigatório para o Hibernate.
     */
    public AppUser() {
    }

    public AppUser(String username, String passwordHash, Role role, String displayName, Long studentId) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.displayName = displayName;
        this.studentId = studentId;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Long getStudentId() {
        return studentId;
    }
}

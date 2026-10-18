package com.studymatch.repository;

import com.studymatch.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositório responsável pelo acesso aos estudantes
 * guardados na base de dados.
 *
 * JpaRepository fornece automaticamente métodos como:
 * save(), findAll(), findById() e deleteById().
 */
public interface StudentRepository extends JpaRepository<Student, Long> {

    // Verifica se já existe um estudante com este email.
    boolean existsByEmail(String email);

    // Verifica se já existe um estudante com este número.
    boolean existsByStudentNumber(String studentNumber);

    // Verifica se outro estudante já utiliza este email.
    boolean existsByEmailAndIdNot(String email, Long id);

    // Verifica se outro estudante já utiliza este número.
    boolean existsByStudentNumberAndIdNot(String studentNumber, Long id);
}
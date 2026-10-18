package com.studymatch.repository;

import com.studymatch.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositório Spring Data JPA para a entidade Quiz.
 */
@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
}
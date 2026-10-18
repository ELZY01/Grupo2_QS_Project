package com.studymatch.repository;

import com.studymatch.model.QuizSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuizSubmissionRepository extends JpaRepository<QuizSubmission, Long> {

    // Retorna a submissão mais recente do estudante (regra de repetição)
    Optional<QuizSubmission> findTopByStudentIdOrderBySubmittedAtDesc(Long studentId);
}
package com.studymatch.security;

/**
 * Sabe se um docente leciona uma UC onde o aluno está inscrito (R3).
 */
public interface TeachingAccess {

    boolean teachesStudent(Long teacherId, Long studentId);
}

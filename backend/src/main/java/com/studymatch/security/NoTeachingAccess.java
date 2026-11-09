package com.studymatch.security;

import org.springframework.stereotype.Component;

/**
 * Enquanto não houver docentes associados a UCs, nenhum docente vê dados de alunos.
 */
@Component
public class NoTeachingAccess implements TeachingAccess {

    @Override
    public boolean teachesStudent(Long teacherId, Long studentId) {
        return false;
    }
}


package com.studymatch.profile;

/**
 * Associa os dados de identificacao de um estudante
 * ao respetivo perfil de competencias.
 */
public record StudentProfileEntry(
        Long studentId,
        String studentNumber,
        String name,
        StudentProfile profile) {
}

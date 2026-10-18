package com.studymatch.profile;

/**
 * Contributo de uma UC para o nível histórico de uma competência (usado na explicação do perfil, US16).
 *
 * @param courseUnitCode código da UC
 * @param courseUnitName nome da UC
 * @param grade          melhor nota aprovada nessa UC
 * @param gradeLevel     essa nota convertida para a escala 1–5
 * @param weight         peso da UC nesta competência
 * @param share          fração do nível histórico que vem desta UC (as frações somam 1)
 */
public record Contribution(
        String courseUnitCode,
        String courseUnitName,
        int grade,
        double gradeLevel,
        double weight,
        double share) {
}

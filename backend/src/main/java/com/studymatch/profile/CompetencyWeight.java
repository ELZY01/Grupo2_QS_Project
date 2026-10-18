package com.studymatch.profile;

/**
 * Quanto uma UC contribui para uma competência.
 *
 * @param competencyCode código da competência (ex.: "PROG")
 * @param courseUnitCode código da UC (ex.: "PROG1")
 * @param weight         peso no intervalo ]0, 1]
 */
public record CompetencyWeight(String competencyCode, String courseUnitCode, double weight) {

    public CompetencyWeight {
        if (competencyCode == null || competencyCode.isBlank()
                || courseUnitCode == null || courseUnitCode.isBlank()) {
            throw new IllegalArgumentException("Competência e UC são obrigatórias.");
        }
        if (!(weight > 0.0 && weight <= 1.0)) {
            throw new IllegalArgumentException(
                    "Peso inválido para " + courseUnitCode + " → " + competencyCode + ": " + weight
                            + " (tem de estar em ]0, 1]).");
        }
    }
}

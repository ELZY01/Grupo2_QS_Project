package com.studymatch.profile;

/**
 * Uma tentativa de um estudante numa unidade curricular, tal como o cálculo do perfil a vê.
 *
 * @param courseUnitCode código da UC (ex.: "PROG1")
 * @param courseUnitName nome da UC, usado na explicação do perfil
 * @param grade          nota de 0 a 20, ou {@code null} se a inscrição ainda está em curso
 */
public record AcademicResult(String courseUnitCode, String courseUnitName, Integer grade) {

    public static final int MIN_GRADE = 0;
    public static final int MAX_GRADE = 20;
    public static final int PASSING_GRADE = 10;

    public AcademicResult {
        if (courseUnitCode == null || courseUnitCode.isBlank()) {
            throw new IllegalArgumentException("O código da UC é obrigatório.");
        }
        if (grade != null && (grade < MIN_GRADE || grade > MAX_GRADE)) {
            throw new IllegalArgumentException(
                    "Nota inválida em " + courseUnitCode + ": " + grade + " (tem de estar entre 0 e 20).");
        }
    }

    /** Aprovado = tem nota e a nota é pelo menos 10. */
    public boolean isApproved() {
        return grade != null && grade >= PASSING_GRADE;
    }
}

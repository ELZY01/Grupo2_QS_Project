package com.studymatch.profile;

/** De onde vem o nível de uma competência. */
public enum LevelOrigin {
    /** Só do quiz inicial (aluno sem notas nas UCs desta competência). */
    QUIZ,
    /** Só das notas (o quiz já não conta, ou o aluno não fez o quiz). */
    HISTORY,
    /** Combinação do quiz com as notas. */
    MIXED,
    /** Não há quiz nem notas: o nível não é calculado. */
    NO_DATA
}

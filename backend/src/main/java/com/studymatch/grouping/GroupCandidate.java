package com.studymatch.grouping;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalDouble;

import com.studymatch.profile.CompetencyLevel;
import com.studymatch.profile.StudentProfile;

/**
 * Um aluno visto pelo algoritmo de formação de grupos (Issue #73, ADR-003 D2).
 *
 * <p>Só guarda o que o algoritmo precisa: a identificação do aluno e o nível (1 a 5)
 * em cada competência. Um nível {@code null} quer dizer "sem dados" (ADR-002 D8):
 * não conta como 0 nem como 1.
 *
 * @param studentId     id do aluno na base de dados
 * @param studentNumber número de estudante (desempate na ordenação, ADR-003 D3)
 * @param name          nome a mostrar no grupo
 * @param levels        nível por código de competência ({@code null} = sem dados)
 */
public record GroupCandidate(Long studentId, String studentNumber, String name, Map<String, Double> levels) {

    public static final double MIN_LEVEL = 1.0;
    public static final double MAX_LEVEL = 5.0;

    /** Média usada só para ordenar um aluno sem dados nenhuns (ADR-003 D5). */
    public static final double NEUTRAL_AVERAGE = 3.0;

    public GroupCandidate {
        Objects.requireNonNull(studentId, "O id do aluno é obrigatório.");
        Objects.requireNonNull(studentNumber, "O número de estudante é obrigatório.");
        Objects.requireNonNull(name, "O nome do aluno é obrigatório.");
        Objects.requireNonNull(levels, "Os níveis do aluno são obrigatórios.");
        for (Map.Entry<String, Double> entry : levels.entrySet()) {
            Double level = entry.getValue();
            if (level != null && (level < MIN_LEVEL || level > MAX_LEVEL)) {
                throw new IllegalArgumentException(
                        "Nível inválido em " + entry.getKey() + ": " + level + " (tem de estar entre 1 e 5).");
            }
        }
        // Map.copyOf não aceita valores null, e null quer dizer "sem dados".
        levels = Collections.unmodifiableMap(new LinkedHashMap<>(levels));
    }

    /**
     * Cria o candidato a partir do perfil calculado pelo {@code ProfileCalculator}.
     */
    public static GroupCandidate fromProfile(Long studentId, String studentNumber, String name,
                                             StudentProfile profile) {
        Objects.requireNonNull(profile, "O perfil do aluno é obrigatório.");
        Map<String, Double> levels = new LinkedHashMap<>();
        for (CompetencyLevel competency : profile.competencies()) {
            levels.put(competency.competencyCode(), competency.level());
        }
        return new GroupCandidate(studentId, studentNumber, name, levels);
    }

    /** Nível numa competência, ou {@code null} se não há dados (ou a competência não existe). */
    public Double level(String competencyCode) {
        return levels.get(competencyCode);
    }

    /** {@code true} se o aluno tem dados em pelo menos uma competência. */
    public boolean hasData() {
        return levels.values().stream().anyMatch(Objects::nonNull);
    }

    /** Média dos níveis com dados (RN15); vazia se o aluno não tem dados nenhuns. */
    public OptionalDouble average() {
        return levels.values().stream()
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average();
    }

    /** Média usada para ordenar: a média real, ou 3,0 se o aluno não tem dados (ADR-003 D5). */
    public double sortingAverage() {
        return average().orElse(NEUTRAL_AVERAGE);
    }
}

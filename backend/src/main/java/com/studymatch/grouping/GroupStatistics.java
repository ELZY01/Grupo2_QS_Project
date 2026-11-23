package com.studymatch.grouping;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Estatísticas de um grupo para a área do docente (Issue #106, R6).
 *
 * <p>Calculadas a partir dos mesmos {@link GroupCandidate} que o algoritmo usa, por isso servem
 * tanto para grupos gerados como para grupos criados ou editados à mão.
 *
 * @param number      nº do grupo
 * @param memberCount nº de membros
 * @param average     média do grupo: média das médias dos membros (sem dados conta 3,0, ADR-003 D5)
 * @param gapToOthers média do grupo − média dos outros grupos do contexto (0 se é o único)
 * @param balanced    {@code true} se |gapToOthers| ≤ 0,5 (RN24)
 * @param areas       estatísticas por competência, pela ordem pedida
 */
public record GroupStatistics(
        int number,
        int memberCount,
        double average,
        double gapToOthers,
        boolean balanced,
        List<AreaStatistics> areas) {

    /** Tolerância nas comparações de médias (arredondamentos de vírgula flutuante). */
    private static final double EPSILON = 1e-9;

    public GroupStatistics {
        Objects.requireNonNull(areas, "As estatísticas por área são obrigatórias.");
        areas = List.copyOf(areas);
    }

    /** Áreas em que ninguém do grupo é forte (lacunas do grupo). */
    public List<String> gaps() {
        return areas.stream().filter(AreaStatistics::isGap).map(AreaStatistics::competencyCode).toList();
    }

    /** Áreas em que nenhum membro tem dados. */
    public List<String> areasWithoutData() {
        return areas.stream().filter(AreaStatistics::hasNoData).map(AreaStatistics::competencyCode).toList();
    }

    /**
     * Estatísticas de todos os grupos de um contexto; os grupos são numerados a partir de 1
     * pela ordem da lista.
     *
     * @throws IllegalArgumentException sem competências, ou com um grupo vazio
     */
    public static List<GroupStatistics> ofAll(List<String> competencyCodes, List<List<GroupCandidate>> groups) {
        if (competencyCodes == null || competencyCodes.isEmpty()) {
            throw new IllegalArgumentException("É preciso indicar pelo menos uma competência.");
        }
        Objects.requireNonNull(groups, "Os grupos são obrigatórios.");

        int k = groups.size();
        double[] averages = new double[k];
        double total = 0;
        for (int i = 0; i < k; i++) {
            List<GroupCandidate> group = groups.get(i);
            if (group == null || group.isEmpty()) {
                throw new IllegalArgumentException("O grupo " + (i + 1) + " não tem membros.");
            }
            averages[i] = SnakeGroupFormation.groupAverage(group);
            total += averages[i];
        }

        List<GroupStatistics> result = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            double gap = k == 1 ? 0 : averages[i] - (total - averages[i]) / (k - 1);
            List<AreaStatistics> areas = new ArrayList<>();
            for (String code : competencyCodes) {
                areas.add(area(code, groups.get(i)));
            }
            result.add(new GroupStatistics(i + 1, groups.get(i).size(), averages[i], gap,
                    Math.abs(gap) <= GroupFormation.MAX_AVERAGE_GAP + EPSILON, areas));
        }
        return result;
    }

    /** Média, mínimo, máximo, nº de fortes e distribuição de uma competência no grupo. */
    static AreaStatistics area(String code, List<GroupCandidate> group) {
        Map<Integer, Integer> distribution = new LinkedHashMap<>();
        for (int level = 1; level <= 5; level++) {
            distribution.put(level, 0);
        }
        double sum = 0;
        Double min = null;
        Double max = null;
        int withData = 0;
        int strong = 0;
        for (GroupCandidate member : group) {
            Double level = member.level(code);
            if (level == null) {
                continue;
            }
            withData++;
            sum += level;
            min = min == null ? level : Math.min(min, level);
            max = max == null ? level : Math.max(max, level);
            if (level >= GroupFormation.STRONG_LEVEL) {
                strong++;
            }
            distribution.merge((int) Math.round(level), 1, Integer::sum);
        }
        Double average = withData == 0 ? null : sum / withData;
        return new AreaStatistics(code, average, min, max, withData, strong, distribution);
    }
}

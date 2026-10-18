package com.studymatch.profile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Calcula o perfil de competências de um estudante (Issue #44, UC06 e UC09).
 *
 * <p>Regras (documentadas no ADR-002):
 * <ol>
 *   <li>Por UC conta só a <b>melhor nota aprovada</b>. Reprovações e inscrições em curso não contam.</li>
 *   <li>Nota → nível: {@code nivel = 1 + 4 × (nota − 10) / 10}, ou seja 10 → 1 e 20 → 5.</li>
 *   <li>Nível histórico de uma competência = média dos níveis das UCs associadas, ponderada pelos pesos.</li>
 *   <li>Peso do quiz: {@code pesoQuiz = max(0, 1 − n/N)}, com n = UCs aprovadas e N = 6 por omissão.</li>
 *   <li>Nível final = {@code pesoQuiz × nivelQuiz + (1 − pesoQuiz) × nivelHistorico}.</li>
 *   <li>Sem histórico na competência → só quiz. Sem quiz → só histórico. Sem nenhum → "sem dados".</li>
 * </ol>
 *
 * <p>Esta classe não depende de Spring nem da base de dados: recebe dados e devolve o perfil.
 * Por isso é testada isoladamente em {@code ProfileCalculatorTest}.
 */
public class ProfileCalculator {

    public static final int DEFAULT_TRANSITION_UNITS = 6;
    public static final double MIN_LEVEL = 1.0;
    public static final double MAX_LEVEL = 5.0;

    private final int transitionUnits;

    public ProfileCalculator() {
        this(DEFAULT_TRANSITION_UNITS);
    }

    /** @param transitionUnits N: nº de UCs aprovadas a partir do qual o quiz deixa de contar */
    public ProfileCalculator(int transitionUnits) {
        if (transitionUnits < 1) {
            throw new IllegalArgumentException("N tem de ser pelo menos 1.");
        }
        this.transitionUnits = transitionUnits;
    }

    /**
     * @param competencyCodes competências a incluir no perfil (todas aparecem, mesmo sem dados)
     * @param results         todas as tentativas do estudante
     * @param weights         associações UC → competência
     * @param quizLevels      resultado do quiz por competência (1–5); vazio se o aluno não fez o quiz
     */
    public StudentProfile calculate(
            List<String> competencyCodes,
            Collection<AcademicResult> results,
            Collection<CompetencyWeight> weights,
            Map<String, Double> quizLevels) {

        quizLevels.forEach((code, value) -> {
            if (value == null || value < MIN_LEVEL || value > MAX_LEVEL) {
                throw new IllegalArgumentException(
                        "Resultado do quiz inválido em " + code + ": " + value + " (tem de estar entre 1 e 5).");
            }
        });

        Map<String, AcademicResult> bestByUnit = bestApprovedResultByUnit(results);
        int completed = bestByUnit.size();
        double globalQuizWeight = quizWeight(completed);

        List<CompetencyLevel> levels = new ArrayList<>();
        for (String code : competencyCodes) {
            List<Contribution> contributions = contributionsFor(code, bestByUnit, weights);
            Double historyLevel = weightedAverage(contributions);
            Double quizLevel = quizLevels.get(code);
            levels.add(combine(code, quizLevel, historyLevel, globalQuizWeight, contributions));
        }
        return new StudentProfile(completed, globalQuizWeight, levels);
    }

    /** {@code max(0, 1 − n/N)}. */
    public double quizWeight(int completedCourseUnits) {
        if (completedCourseUnits < 0) {
            throw new IllegalArgumentException("O nº de UCs concluídas não pode ser negativo.");
        }
        return Math.max(0.0, 1.0 - (double) completedCourseUnits / transitionUnits);
    }

    /** Converte uma nota aprovada (10–20) para a escala 1–5. */
    public static double gradeToLevel(int grade) {
        if (grade < AcademicResult.PASSING_GRADE || grade > AcademicResult.MAX_GRADE) {
            throw new IllegalArgumentException("Só notas aprovadas (10–20) têm nível: " + grade);
        }
        return MIN_LEVEL + (MAX_LEVEL - MIN_LEVEL) * (grade - AcademicResult.PASSING_GRADE) / 10.0;
    }

    private static Map<String, AcademicResult> bestApprovedResultByUnit(Collection<AcademicResult> results) {
        Map<String, AcademicResult> best = new LinkedHashMap<>();
        for (AcademicResult r : results) {
            if (!r.isApproved()) {
                continue;
            }
            best.merge(r.courseUnitCode(), r, (a, b) -> a.grade() >= b.grade() ? a : b);
        }
        return best;
    }

    private static List<Contribution> contributionsFor(
            String competencyCode,
            Map<String, AcademicResult> bestByUnit,
            Collection<CompetencyWeight> weights) {

        List<CompetencyWeight> relevant = weights.stream()
                .filter(w -> w.competencyCode().equals(competencyCode))
                .filter(w -> bestByUnit.containsKey(w.courseUnitCode()))
                .toList();
        double totalWeight = relevant.stream().mapToDouble(CompetencyWeight::weight).sum();

        List<Contribution> contributions = new ArrayList<>();
        for (CompetencyWeight w : relevant) {
            AcademicResult r = bestByUnit.get(w.courseUnitCode());
            contributions.add(new Contribution(
                    r.courseUnitCode(),
                    r.courseUnitName(),
                    r.grade(),
                    gradeToLevel(r.grade()),
                    w.weight(),
                    w.weight() / totalWeight));
        }
        return contributions;
    }

    private static Double weightedAverage(List<Contribution> contributions) {
        if (contributions.isEmpty()) {
            return null;
        }
        return contributions.stream().mapToDouble(c -> c.share() * c.gradeLevel()).sum();
    }

    private static CompetencyLevel combine(
            String code, Double quizLevel, Double historyLevel, double globalQuizWeight,
            List<Contribution> contributions) {

        if (quizLevel == null && historyLevel == null) {
            return new CompetencyLevel(code, null, LevelOrigin.NO_DATA, 0.0, null, null, contributions);
        }
        if (historyLevel == null) {
            return new CompetencyLevel(code, quizLevel, LevelOrigin.QUIZ, 1.0, quizLevel, null, contributions);
        }
        if (quizLevel == null || globalQuizWeight == 0.0) {
            return new CompetencyLevel(code, historyLevel, LevelOrigin.HISTORY, 0.0, quizLevel, historyLevel,
                    contributions);
        }
        double level = globalQuizWeight * quizLevel + (1.0 - globalQuizWeight) * historyLevel;
        return new CompetencyLevel(code, level, LevelOrigin.MIXED, globalQuizWeight, quizLevel, historyLevel,
                contributions);
    }
}

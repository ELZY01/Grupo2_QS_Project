package com.studymatch.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Testes das regras do perfil de competências (Issue #44).
 * Não usam Spring nem base de dados: correm em milissegundos com {@code mvnw test}.
 */
class ProfileCalculatorTest {

    private static final double EPS = 1e-9;

    private static final List<String> COMPETENCIES = List.of("ORG", "TEMPO", "PROG", "EQUIPA");

    /** Os mesmos pesos que estão em V3__insert_course_units_and_weights.sql. */
    private static final List<CompetencyWeight> WEIGHTS = List.of(
            new CompetencyWeight("PROG", "PROG1", 1.0),
            new CompetencyWeight("ORG", "PROG1", 0.3),
            new CompetencyWeight("PROG", "BD", 0.8),
            new CompetencyWeight("ORG", "BD", 0.5),
            new CompetencyWeight("EQUIPA", "ES", 1.0),
            new CompetencyWeight("TEMPO", "ES", 0.8),
            new CompetencyWeight("ORG", "ES", 0.7),
            new CompetencyWeight("PROG", "POO", 1.0),
            new CompetencyWeight("EQUIPA", "POO", 0.4));

    private final ProfileCalculator calculator = new ProfileCalculator();

    private static AcademicResult uc(String code, Integer grade) {
        return new AcademicResult(code, "UC " + code, grade);
    }

    private StudentProfile profile(List<AcademicResult> results, Map<String, Double> quiz) {
        return calculator.calculate(COMPETENCIES, results, WEIGHTS, quiz);
    }

    private static CompetencyLevel level(StudentProfile p, String code) {
        return p.competency(code).orElseThrow();
    }

    @Nested
    @DisplayName("Conversão nota → nível")
    class GradeToLevel {

        @ParameterizedTest(name = "nota {0} → nível {1}")
        @CsvSource({"10, 1.0", "15, 3.0", "20, 5.0", "12, 1.8"})
        void convertePelaFormula(int grade, double expected) {
            assertEquals(expected, ProfileCalculator.gradeToLevel(grade), EPS);
        }

        @ParameterizedTest(name = "nota {0} não tem nível")
        @ValueSource(ints = {9, 0, 21})
        void rejeitaNotasNaoAprovadasOuForaDoIntervalo(int grade) {
            assertThrows(IllegalArgumentException.class, () -> ProfileCalculator.gradeToLevel(grade));
        }
    }

    @Nested
    @DisplayName("Perfil a partir do histórico (cenário A)")
    class History {

        @Test
        @DisplayName("Cenário normal: média ponderada pelos pesos das UCs")
        void mediaPonderadaPelosPesos() {
            // PROG: PROG1 = 20 (nível 5, peso 1.0) e BD = 10 (nível 1, peso 0.8)
            // → (1.0 × 5 + 0.8 × 1) / 1.8 = 3.2222…
            StudentProfile p = profile(List.of(uc("PROG1", 20), uc("BD", 10)), Map.of());

            CompetencyLevel prog = level(p, "PROG");
            assertEquals(5.8 / 1.8, prog.level(), EPS);
            assertEquals(LevelOrigin.HISTORY, prog.origin());
            assertEquals(2, prog.contributions().size());
            assertEquals(1.0, prog.contributions().stream().mapToDouble(Contribution::share).sum(), EPS);
        }

        @Test
        @DisplayName("Regra do perfil: conta a melhor tentativa aprovada")
        void contaAMelhorTentativa() {
            StudentProfile p = profile(List.of(uc("PROG1", 8), uc("PROG1", 12), uc("PROG1", 16)), Map.of());

            CompetencyLevel prog = level(p, "PROG");
            assertEquals(ProfileCalculator.gradeToLevel(16), prog.level(), EPS);
            assertEquals(16, prog.contributions().get(0).grade());
            assertEquals(1, p.completedCourseUnits(), "três tentativas na mesma UC contam como 1 UC concluída");
        }

        @Test
        @DisplayName("Reprovações e inscrições em curso não contam")
        void reprovacoesEEmCursoNaoContam() {
            StudentProfile p = profile(List.of(uc("PROG1", 9), uc("BD", null)), Map.of());

            assertEquals(0, p.completedCourseUnits());
            assertEquals(LevelOrigin.NO_DATA, level(p, "PROG").origin());
        }

        @Test
        @DisplayName("Ao acrescentar uma nota, o perfil atualiza-se")
        void novaNotaAtualizaOPerfil() {
            double antes = level(profile(List.of(uc("PROG1", 12)), Map.of()), "PROG").level();
            double depois = level(profile(List.of(uc("PROG1", 12), uc("POO", 20)), Map.of()), "PROG").level();

            assertEquals(1.8, antes, EPS);
            assertEquals((1.8 + 5.0) / 2, depois, EPS);
        }
    }

    @Nested
    @DisplayName("Arranque a frio e transição (cenário B)")
    class ColdStart {

        private final Map<String, Double> quiz = Map.of("ORG", 4.0, "TEMPO", 3.0, "PROG", 4.0, "EQUIPA", 2.0);

        @Test
        @DisplayName("Regra do cold start: sem notas, o perfil é 100% quiz")
        void semNotasEhSoQuiz() {
            StudentProfile p = profile(List.of(), quiz);

            assertEquals(1.0, p.quizWeight(), EPS);
            for (String code : COMPETENCIES) {
                CompetencyLevel c = level(p, code);
                assertEquals(LevelOrigin.QUIZ, c.origin());
                assertEquals(quiz.get(code), c.level(), EPS);
            }
        }

        @Test
        @DisplayName("Exemplo do UC09: 3 UCs → pesoQuiz 0,5; quiz 4 e histórico 3 → 3,5")
        void exemploDoUc09() {
            // PROG1, BD e POO aprovadas com 15 (nível 3) → nível histórico de PROG = 3
            StudentProfile p = profile(List.of(uc("PROG1", 15), uc("BD", 15), uc("POO", 15)), quiz);

            CompetencyLevel prog = level(p, "PROG");
            assertEquals(0.5, p.quizWeight(), EPS);
            assertEquals(LevelOrigin.MIXED, prog.origin());
            assertEquals(3.5, prog.level(), EPS);
        }

        @ParameterizedTest(name = "{0} UCs concluídas → pesoQuiz {1}")
        @CsvSource({"0, 1.0", "1, 0.8333333333", "3, 0.5", "5, 0.1666666667", "6, 0.0", "7, 0.0", "20, 0.0"})
        @DisplayName("Limites da transição: o peso nunca é negativo")
        void limitesDoPesoDoQuiz(int completed, double expected) {
            assertEquals(expected, calculator.quizWeight(completed), 1e-9);
        }

        @Test
        @DisplayName("Com N UCs concluídas, o quiz deixa de contar")
        void comNUcsEhSoHistorico() {
            List<AcademicResult> six = List.of(uc("PROG1", 15), uc("BD", 15), uc("POO", 15),
                    uc("ES", 15), uc("X1", 15), uc("X2", 15));
            CompetencyLevel prog = level(profile(six, quiz), "PROG");

            assertEquals(LevelOrigin.HISTORY, prog.origin());
            assertEquals(3.0, prog.level(), EPS);
            assertEquals(0.0, prog.quizWeight(), EPS);
        }

        @Test
        @DisplayName("Competência sem notas usa só o quiz, mesmo com outras UCs concluídas")
        void competenciaSemHistoricoUsaQuiz() {
            // PROG1 só alimenta PROG e ORG; EQUIPA continua sem histórico
            CompetencyLevel equipa = level(profile(List.of(uc("PROG1", 18)), quiz), "EQUIPA");

            assertEquals(LevelOrigin.QUIZ, equipa.origin());
            assertEquals(2.0, equipa.level(), EPS);
            assertEquals(1.0, equipa.quizWeight(), EPS);
        }
    }

    @Nested
    @DisplayName("Dados em falta e dados inválidos")
    class MissingAndInvalid {

        @Test
        @DisplayName("Sem quiz nem notas: todas as competências ficam 'sem dados'")
        void semDadosNenhuns() {
            StudentProfile p = profile(List.of(), Map.of());

            assertEquals(4, p.competencies().size());
            for (CompetencyLevel c : p.competencies()) {
                assertEquals(LevelOrigin.NO_DATA, c.origin());
                assertNull(c.level());
            }
        }

        @ParameterizedTest(name = "nota {0} é rejeitada")
        @ValueSource(ints = {-1, 21, 25})
        void notaForaDoIntervalo(int grade) {
            assertThrows(IllegalArgumentException.class, () -> uc("PROG1", grade));
        }

        @ParameterizedTest(name = "peso {0} é rejeitado")
        @ValueSource(doubles = {0.0, -0.5, 1.5})
        void pesoForaDoIntervalo(double weight) {
            assertThrows(IllegalArgumentException.class, () -> new CompetencyWeight("PROG", "PROG1", weight));
        }

        @ParameterizedTest(name = "resultado do quiz {0} é rejeitado")
        @ValueSource(doubles = {0.0, 0.99, 5.01, 6.0})
        void quizForaDoIntervalo(double value) {
            assertThrows(IllegalArgumentException.class, () -> profile(List.of(), Map.of("PROG", value)));
        }
    }
}

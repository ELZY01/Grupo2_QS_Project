package com.studymatch.grouping;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.studymatch.profile.CompetencyLevel;
import com.studymatch.profile.LevelOrigin;
import com.studymatch.profile.StudentProfile;

/**
 * Testes do aluno visto pelo algoritmo de grupos (Issue #73, ADR-003 D2 e D5).
 */
class GroupCandidateTest {

    private static final double EPS = 1e-9;

    private static Map<String, Double> levels(Double org, Double tempo, Double prog, Double equipa) {
        Map<String, Double> levels = new HashMap<>();
        levels.put("ORG", org);
        levels.put("TEMPO", tempo);
        levels.put("PROG", prog);
        levels.put("EQUIPA", equipa);
        return levels;
    }

    private static GroupCandidate candidate(Map<String, Double> levels) {
        return new GroupCandidate(1L, "20260001", "Ana", levels);
    }

    @Test
    @DisplayName("A média é a média dos níveis com dados")
    void averageOfAllLevels() {
        GroupCandidate ana = candidate(levels(4.0, 3.0, 5.0, 2.0));

        assertEquals(3.5, ana.average().getAsDouble(), EPS);
        assertEquals(3.5, ana.sortingAverage(), EPS);
        assertTrue(ana.hasData());
    }

    @Test
    @DisplayName("Uma competência sem dados não conta como 0 na média")
    void missingLevelIsIgnored() {
        GroupCandidate ana = candidate(levels(4.0, null, 2.0, null));

        assertEquals(3.0, ana.average().getAsDouble(), EPS);
        assertNull(ana.level("TEMPO"));
    }

    @Test
    @DisplayName("Sem dados nenhuns: média vazia e 3,0 só para ordenar")
    void noDataUsesNeutralAverageForSorting() {
        GroupCandidate ana = candidate(levels(null, null, null, null));

        assertFalse(ana.hasData());
        assertTrue(ana.average().isEmpty());
        assertEquals(GroupCandidate.NEUTRAL_AVERAGE, ana.sortingAverage(), EPS);
    }

    @Test
    @DisplayName("Os limites 1 e 5 são aceites")
    void boundaryLevelsAreAccepted() {
        GroupCandidate ana = candidate(levels(1.0, 5.0, 1.0, 5.0));

        assertEquals(3.0, ana.average().getAsDouble(), EPS);
    }

    @Test
    @DisplayName("Níveis fora de 1 a 5 são rejeitados")
    void levelOutOfRangeIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> candidate(levels(0.9, 3.0, 3.0, 3.0)));
        assertThrows(IllegalArgumentException.class, () -> candidate(levels(3.0, 5.1, 3.0, 3.0)));
    }

    @Test
    @DisplayName("Id, número, nome e níveis são obrigatórios")
    void requiredFields() {
        Map<String, Double> ok = levels(3.0, 3.0, 3.0, 3.0);

        assertThrows(NullPointerException.class, () -> new GroupCandidate(null, "1", "Ana", ok));
        assertThrows(NullPointerException.class, () -> new GroupCandidate(1L, null, "Ana", ok));
        assertThrows(NullPointerException.class, () -> new GroupCandidate(1L, "1", null, ok));
        assertThrows(NullPointerException.class, () -> new GroupCandidate(1L, "1", "Ana", null));
    }

    @Test
    @DisplayName("Os níveis não mudam se o mapa original mudar")
    void levelsAreCopied() {
        Map<String, Double> original = levels(3.0, 3.0, 3.0, 3.0);
        GroupCandidate ana = candidate(original);

        original.put("ORG", 5.0);

        assertEquals(3.0, ana.level("ORG"), EPS);
        assertThrows(UnsupportedOperationException.class, () -> ana.levels().put("ORG", 1.0));
    }

    @Test
    @DisplayName("Converte o perfil do ProfileCalculator, mantendo o \"sem dados\"")
    void fromProfile() {
        StudentProfile profile = new StudentProfile(1, 5.0 / 6, List.of(
                new CompetencyLevel("ORG", 4.0, LevelOrigin.QUIZ, 1.0, 4.0, null, List.of()),
                new CompetencyLevel("PROG", null, LevelOrigin.NO_DATA, 0.0, null, null, List.of())));

        GroupCandidate ana = GroupCandidate.fromProfile(7L, "20260007", "Ana", profile);

        assertEquals(7L, ana.studentId().longValue());
        assertEquals(4.0, ana.level("ORG"), EPS);
        assertNull(ana.level("PROG"));
        assertEquals(4.0, ana.average().getAsDouble(), EPS);
    }
}

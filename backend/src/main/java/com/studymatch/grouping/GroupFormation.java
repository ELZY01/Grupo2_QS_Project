package com.studymatch.grouping;

import java.util.List;

/**
 * Estratégia de formação automática de grupos (Issue #73, ADR-003).
 *
 * <p>É uma interface para que o service da API (#69) dependa só deste contrato e o possa
 * simular nos testes. A implementação aprovada (serpentina + complementaridade) é feita
 * nas Issues #70 (tamanhos) e #71 (distribuição), em Java puro, sem Spring nem base de dados.
 *
 * <p>Regras que qualquer implementação tem de cumprir (enunciado do Sprint 3, secção 3.1):
 * <ul>
 *   <li>todos os candidatos ficam num grupo;</li>
 *   <li>cada candidato fica num só grupo, e nunca repetido;</li>
 *   <li>grupos de {@value #MIN_GROUP_SIZE} ou {@value #MAX_GROUP_SIZE} alunos;</li>
 *   <li>configuração impossível → {@link ImpossibleGroupConfigurationException}.</li>
 * </ul>
 */
public interface GroupFormation {

    int MIN_GROUP_SIZE = 4;
    int MAX_GROUP_SIZE = 5;

    /** Limite de equilíbrio entre médias de grupos (RN24, ADR-003 D3). */
    double MAX_AVERAGE_GAP = 0.5;

    /** Nível a partir do qual um aluno conta como forte numa área (ADR-003 D4). */
    double STRONG_LEVEL = 3.5;

    /**
     * Distribui os candidatos por grupos.
     *
     * @param competencyCodes competências a considerar (ex.: ORG, TEMPO, PROG, EQUIPA)
     * @param candidates      alunos elegíveis do contexto
     * @return os grupos formados e a sua explicação
     * @throws ImpossibleGroupConfigurationException se não há divisão em grupos de 4 ou 5,
     *                                               ou se há alunos repetidos
     */
    GroupFormationResult form(List<String> competencyCodes, List<GroupCandidate> candidates);
}

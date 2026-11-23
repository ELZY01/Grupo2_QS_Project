package com.studymatch.dto;

import com.studymatch.grouping.AreaStatistics;

import java.util.List;

/**
 * Grupos atuais de um contexto com as estatísticas de cada um (Issue #106, R6).
 *
 * <p>"Atuais" quer dizer os da geração mais recente: é essa que o docente vê e edita.
 *
 * @param contextId        ID do contexto
 * @param contextName      nome da atividade
 * @param generationNumber geração mostrada, ou {@code null} se ainda não há grupos
 * @param maxAverageGap    limite de equilíbrio entre médias de grupos (RN24: 0,5)
 * @param averageSpread    diferença entre a maior e a menor média de grupo
 * @param balanced         {@code true} se {@code averageSpread} ≤ {@code maxAverageGap}
 * @param groups           grupos, pelo seu número
 * @param unassigned       alunos do contexto que não estão em nenhum grupo desta geração
 */
public record TeacherGroupsResponse(
        Long contextId,
        String contextName,
        Integer generationNumber,
        double maxAverageGap,
        double averageSpread,
        boolean balanced,
        List<GroupView> groups,
        List<GeneratedGroupResponse.GroupStudentResponse> unassigned) {

    public TeacherGroupsResponse {
        groups = List.copyOf(groups);
        unassigned = List.copyOf(unassigned);
    }

    /**
     * Um grupo e as suas estatísticas (R6).
     *
     * @param id              ID do grupo (para editar ou apagar)
     * @param number          nº do grupo no contexto
     * @param memberCount     nº de membros
     * @param average         média do grupo (aluno sem dados conta 3,0, ADR-003 D5)
     * @param gapToOthers     média do grupo − média dos outros grupos
     * @param balanced        {@code true} se |gapToOthers| ≤ 0,5
     * @param gaps            áreas sem ninguém forte (nível ≥ 3,5)
     * @param areas           média, mínimo, máximo, fortes e distribuição por competência
     * @param members         alunos do grupo
     * @param strongestByArea membro mais forte em cada área (ADR-003 D6)
     */
    public record GroupView(
            Long id,
            int number,
            int memberCount,
            double average,
            double gapToOthers,
            boolean balanced,
            List<String> gaps,
            List<AreaStatistics> areas,
            List<GeneratedGroupResponse.GroupStudentResponse> members,
            List<Strongest> strongestByArea) {

        public GroupView {
            gaps = List.copyOf(gaps);
            areas = List.copyOf(areas);
            members = List.copyOf(members);
            strongestByArea = List.copyOf(strongestByArea);
        }
    }

    /**
     * Membro mais forte numa área.
     *
     * @param area        código da competência (ex.: PROG)
     * @param studentName nome do aluno
     */
    public record Strongest(String area, String studentName) {
    }
}

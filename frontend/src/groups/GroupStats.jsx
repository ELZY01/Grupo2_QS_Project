import { AREAS } from '../mascot/mascot.js';
import { formatDecimal } from './groupPlan.js';

const LEVELS = [1, 2, 3, 4, 5];

const areaLabel = (code) => AREAS[code]?.label ?? code;

/**
 * Estatísticas de um grupo na área do docente (#106, R6): nº de membros, equilíbrio,
 * lacunas e, por competência, a média, o mínimo e o máximo, os alunos fortes (nível ≥ 3,5)
 * e quantos alunos há em cada nível de 1 a 5.
 *
 * @param {{ group: object }} props grupo vindo de /api/teacher/.../groups
 */
export default function GroupStats({ group }) {
  const members = `${group.memberCount} ${group.memberCount === 1 ? 'membro' : 'membros'}`;
  const gaps = group.gaps ?? [];

  return (
    <div className="group-stats">
      <h4>Estatísticas do grupo</h4>
      <p className="group-stats-summary">
        {members} ·{' '}
        <span className={group.balanced ? 'ok' : 'warn'}>
          {group.balanced ? 'equilibrado' : 'desequilibrado'}
        </span>
      </p>
      <p className={`group-gaps ${gaps.length > 0 ? 'warn' : 'ok'}`}>
        {gaps.length === 0
          ? 'Sem lacunas: há alguém forte em todas as áreas.'
          : `Lacunas (ninguém forte): ${gaps.map(areaLabel).join(', ')}`}
      </p>

      <div className="group-stats-scroll">
        <table className="group-stats-table">
          <caption className="visually-hidden">Competências do grupo {group.number}</caption>
          <thead>
            <tr>
              <th scope="col">Área</th>
              <th scope="col">Média</th>
              <th scope="col">Mín.–máx.</th>
              <th scope="col">Fortes</th>
              <th scope="col" title="Nº de alunos em cada nível, de 1 a 5">Níveis 1–5</th>
            </tr>
          </thead>
          <tbody>
            {group.areas.map((area) => (
              <tr key={area.competencyCode}>
                <th scope="row">{areaLabel(area.competencyCode)}</th>
                <td>{area.average == null ? 'sem dados' : formatDecimal(area.average)}</td>
                <td>{area.min == null ? '—' : `${formatDecimal(area.min)}–${formatDecimal(area.max)}`}</td>
                <td>{area.strongMembers}</td>
                <td className="group-levels">
                  {LEVELS.map((level) => area.distribution?.[level] ?? 0).join(' · ')}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

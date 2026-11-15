import { useState } from 'react';
import { AREAS } from '../mascot/mascot.js';
import { formatDecimal, formatGap, swapError } from './groupPlan.js';

/**
 * Ajuste manual (#75): escolher dois alunos de grupos diferentes e trocá-los.
 * As médias novas vêm do backend (o ecrã não conhece os níveis dos alunos, RN26).
 */
function SwapPanel({ groups, onSwap }) {
  const [studentA, setStudentA] = useState('');
  const [studentB, setStudentB] = useState('');
  const [error, setError] = useState(null);
  const [swapping, setSwapping] = useState(false);

  const options = groups.flatMap((g) =>
    g.members.map((m) => ({ id: m.studentId, label: `G${g.number} · ${m.name}` })));

  const handleSubmit = async (event) => {
    event.preventDefault();
    const a = studentA === '' ? null : Number(studentA);
    const b = studentB === '' ? null : Number(studentB);
    const message = swapError(groups, a, b);
    setError(message);
    if (message) return;

    setSwapping(true);
    try {
      await onSwap(a, b);
      setStudentA('');
      setStudentB('');
    } catch (err) {
      setError(err.message);
    } finally {
      setSwapping(false);
    }
  };

  const select = (label, value, setValue) => (
    <label>
      {label}
      <select value={value} onChange={(e) => setValue(e.target.value)}>
        <option value="">—</option>
        {options.map((o) => (
          <option key={o.id} value={o.id}>{o.label}</option>
        ))}
      </select>
    </label>
  );

  return (
    <form className="group-swap" onSubmit={handleSubmit} aria-label="Trocar alunos">
      <h3>Ajuste manual</h3>
      <div className="group-swap-fields">
        {select('Aluno', studentA, setStudentA)}
        {select('Trocar com', studentB, setStudentB)}
        <button type="submit" className="btn-primary" disabled={swapping}>
          {swapping ? 'A trocar…' : 'Trocar'}
        </button>
      </div>
      {error && <p className="group-error" role="alert">{error}</p>}
    </form>
  );
}

/**
 * Grupos de uma geração e porque é que os alunos ficaram juntos.
 * RN26: só mostra nomes, a média do grupo e o membro mais forte por área.
 * Nunca mostra notas nem níveis individuais.
 */
export default function GroupResults({ generation, onSwap }) {
  const { groups = [], maxAverageGap } = generation;

  if (groups.length === 0) {
    return <p>Esta geração não tem grupos.</p>;
  }

  const averages = groups.map((g) => g.average);
  const spread = Math.max(...averages) - Math.min(...averages);
  const balanced = maxAverageGap == null || spread <= maxAverageGap + 1e-9;

  return (
    <div className="group-results">
      <p className={`group-balance ${balanced ? 'ok' : 'warn'}`} role={balanced ? undefined : 'alert'}>
        Diferença entre o grupo mais forte e o mais fraco: <strong>{formatDecimal(spread)}</strong>
        {maxAverageGap != null && ` (limite ${formatDecimal(maxAverageGap)})`}
        {balanced ? ' · equilibrado' : ' · acima do limite'}
      </p>

      <p className="group-method">
        Os alunos foram ordenados pela média do perfil e distribuídos em serpentina
        (G1, G2, …, G2, G1). Depois houve trocas para cada grupo ter alguém forte em cada área.
      </p>

      <div className="group-grid">
        {groups.map((group) => (
          <article key={group.number} className="card group-card" aria-label={`Grupo ${group.number}`}>
            <header className="group-card-header">
              <h3>Grupo {group.number}</h3>
              <span className="group-average">média {formatDecimal(group.average)} / 5</span>
            </header>

            <p className="group-gap">
              {formatGap(group.gapToOthers)} em relação à média dos outros grupos
            </p>

            <h4>Alunos</h4>
            <ul className="group-members">
              {group.members.map((member) => (
                <li key={member.studentId}>
                  {member.name}
                  {member.studentNumber && <span className="group-number">{member.studentNumber}</span>}
                </li>
              ))}
            </ul>

            <h4>Mais forte por área</h4>
            <ul className="group-strongest">
              {Object.entries(AREAS).map(([code, area]) => {
                const strongest = group.strongestByArea?.find((s) => s.area === code);
                return (
                  <li key={code} className={`group-area group-area-${area.key}`}>
                    <span className="group-area-label">{area.label}</span>
                    <span>{strongest?.studentName ?? 'sem dados'}</span>
                  </li>
                );
              })}
            </ul>
          </article>
        ))}
      </div>

      {onSwap && <SwapPanel groups={groups} onSwap={onSwap} />}
    </div>
  );
}

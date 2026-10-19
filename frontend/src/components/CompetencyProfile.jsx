import { useCallback, useEffect, useState } from 'react';
import { getProfile } from '../api/profileApi';

const ORIGIN = {
  QUIZ: { label: 'Quiz', text: 'Vem só do quiz inicial: ainda não há notas nas UCs desta competência.' },
  HISTORY: { label: 'Histórico', text: 'Vem só das notas: o quiz já não conta para esta competência.' },
  MIXED: { label: 'Misto', text: 'Combina o quiz inicial com as notas das UCs.' },
  NO_DATA: { label: 'Sem dados', text: 'Ainda não há quiz nem notas para calcular esta competência.' },
};

const decimal = (value) =>
  value == null ? '—' : value.toLocaleString('pt-PT', { minimumFractionDigits: 1, maximumFractionDigits: 1 });
const percent = (value) => `${Math.round(value * 100)}%`;

/**
 * Perfil de competências de um estudante e a explicação de cada nível
 * (Issue #50, UC07 e US16).
 *
 * @param {number} studentId estudante selecionado
 * @param {number} refreshKey muda quando o histórico é alterado, para recalcular o perfil
 */
export default function CompetencyProfile({ studentId, refreshKey = 0 }) {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setProfile(await getProfile(studentId));
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [studentId]);

  useEffect(() => {
    load();
  }, [load, refreshKey]);

  return (
    <section className="record-card" aria-labelledby="profile-title">
      <h3 id="profile-title">Perfil de competências</h3>

      {loading && !profile && <p>A calcular o perfil…</p>}

      {error && (
        <p className="record-error" role="alert">
          Não foi possível carregar o perfil ({error}).
        </p>
      )}

      {profile && !error && (
        <>
          <p className="profile-summary">
            {profile.completedCourseUnits} UC(s) concluída(s) · o quiz conta{' '}
            <strong>{percent(profile.quizWeight)}</strong> no perfil
          </p>

          {!profile.quizCompleted && (
            <p className="profile-hint">
              Ainda não respondeste ao quiz inicial. Faz o quiz para teres um perfil desde já.
            </p>
          )}

          <ul className="competency-list">
            {profile.competencies.map((c) => (
              <CompetencyItem key={c.code} competency={c} />
            ))}
          </ul>
        </>
      )}
    </section>
  );
}

function CompetencyItem({ competency: c }) {
  const origin = ORIGIN[c.origin] ?? ORIGIN.NO_DATA;
  const noData = c.origin === 'NO_DATA' || c.level == null;
  const width = noData ? 0 : Math.max(0, Math.min(100, (c.level / 5) * 100));

  return (
    <li className="competency">
      <details>
        <summary>
          <span className="competency-name">{c.name}</span>
          <span className="competency-bar" aria-hidden="true">
            <span style={{ width: `${width}%` }} />
          </span>
          <span className="competency-level">{noData ? '—' : `${decimal(c.level)} / 5`}</span>
          <span className={`origin origin-${c.origin.toLowerCase()}`}>{origin.label}</span>
        </summary>

        <div className="competency-detail">
          <p>{origin.text}</p>

          {noData ? (
            <p className="profile-hint">
              Para teres um nível aqui, faz o quiz inicial ou regista notas nas UCs desta competência.
            </p>
          ) : (
            <>
              <dl className="competency-facts">
                <dt>Quiz</dt>
                <dd>
                  {decimal(c.quizLevel)} · conta {percent(c.quizWeight)}
                </dd>
                <dt>Notas</dt>
                <dd>
                  {decimal(c.historyLevel)} · conta {percent(1 - c.quizWeight)}
                </dd>
              </dl>

              {c.contributions.length > 0 && (
                <div className="record-table-wrap">
                  <table className="record-table">
                    <caption>UCs que contam para esta competência</caption>
                    <thead>
                      <tr>
                        <th>UC</th>
                        <th>Melhor nota</th>
                        <th>Nível (1–5)</th>
                        <th>Peso</th>
                        <th>Parte do nível</th>
                      </tr>
                    </thead>
                    <tbody>
                      {c.contributions.map((u) => (
                        <tr key={u.courseUnitCode}>
                          <td>
                            {u.courseUnitCode} · {u.courseUnitName}
                          </td>
                          <td>{u.grade}</td>
                          <td>{decimal(u.gradeLevel)}</td>
                          <td>{decimal(u.weight)}</td>
                          <td>{percent(u.share)}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </>
          )}
        </div>
      </details>
    </li>
  );
}

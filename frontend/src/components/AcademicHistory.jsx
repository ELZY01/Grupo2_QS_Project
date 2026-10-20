import { useCallback, useEffect, useMemo, useState } from 'react';
import { createEnrollment, getCourseUnits, getEnrollments } from '../api/enrollmentApi';
import EnrollmentForm from './EnrollmentForm';

const STATUS_LABEL = {
  APROVADO: 'Aprovado',
  REPROVADO: 'Reprovado',
  EM_CURSO: 'Em curso',
};

/**
 * Histórico académico de um estudante (Issue #49, UC03 e UC04):
 * regista inscrições e mostra todas as tentativas por ano letivo e semestre.
 *
 * @param {number} studentId estudante selecionado
 * @param {Function} onChange chamado depois de registar uma inscrição (ex.: para recalcular o perfil)
 * @param {boolean} readOnly só mostra o histórico (o estudante vê; quem regista as UCs é o administrador)
 */
export default function AcademicHistory({ studentId, onChange, readOnly = false }) {
  const [courseUnits, setCourseUnits] = useState([]);
  const [enrollments, setEnrollments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState(null);
  const [formError, setFormError] = useState(null);
  const [saving, setSaving] = useState(false);
  const [success, setSuccess] = useState(null);

  const load = useCallback(async () => {
    setLoading(true);
    setLoadError(null);
    try {
      const [units, history] = await Promise.all([getCourseUnits(), getEnrollments(studentId)]);
      setCourseUnits(units);
      setEnrollments(history);
    } catch (error) {
      setLoadError(error.message);
    } finally {
      setLoading(false);
    }
  }, [studentId]);

  useEffect(() => {
    setFormError(null);
    setSuccess(null);
    load();
  }, [load]);

  const unitsById = useMemo(
    () => Object.fromEntries(courseUnits.map((uc) => [uc.id, uc])),
    [courseUnits]
  );

  // Ordena por ano letivo, semestre, UC e tentativa.
  const rows = useMemo(
    () =>
      [...enrollments].sort(
        (a, b) =>
          a.schoolYear - b.schoolYear ||
          a.semester - b.semester ||
          a.courseUnitId - b.courseUnitId ||
          a.attemptNumber - b.attemptNumber
      ),
    [enrollments]
  );

  const handleSubmit = async (data) => {
    setSaving(true);
    setFormError(null);
    setSuccess(null);
    try {
      const created = await createEnrollment(studentId, data);
      setEnrollments((list) => [...list, created]);
      const uc = unitsById[created.courseUnitId];
      setSuccess(`Inscrição registada: ${uc ? uc.name : 'UC'}, tentativa ${created.attemptNumber}.`);
      onChange?.();
      return true;
    } catch (error) {
      // Mostra o erro de validação devolvido pela API (ex.: nota fora de 0–20).
      setFormError(error.message);
      return false;
    } finally {
      setSaving(false);
    }
  };

  return (
    <section className="record-card" aria-labelledby="history-title">
      <h3 id="history-title">Histórico académico</h3>

      {loading && <p>A carregar o histórico…</p>}

      {!loading && loadError && (
        <p className="record-error" role="alert">
          Não foi possível carregar o histórico ({loadError}).
        </p>
      )}

      {!loading && !loadError && (
        <>
          {!readOnly && (
            <EnrollmentForm
              courseUnits={courseUnits}
              onSubmit={handleSubmit}
              error={formError}
              saving={saving}
            />
          )}

          {success && (
            <p className="record-success" role="status">
              {success}
            </p>
          )}

          {rows.length === 0 ? (
            <p className="record-empty">
              {readOnly
                ? 'Ainda não tens cadeiras registadas. Quando o administrador as lançar, aparecem aqui.'
                : 'Ainda não há inscrições registadas para este estudante.'}
            </p>
          ) : (
            <div className="record-table-wrap">
              <table className="record-table">
                <thead>
                  <tr>
                    <th>Ano letivo</th>
                    <th>Semestre</th>
                    <th>Unidade curricular</th>
                    <th>Tentativa</th>
                    <th>Nota</th>
                    <th>Estado</th>
                  </tr>
                </thead>
                <tbody>
                  {rows.map((e) => {
                    const uc = unitsById[e.courseUnitId];
                    return (
                      <tr key={e.id}>
                        <td>
                          {e.schoolYear}/{e.schoolYear + 1}
                        </td>
                        <td>{e.semester}.º</td>
                        <td>{uc ? `${uc.code} · ${uc.name}` : `UC ${e.courseUnitId}`}</td>
                        <td>{e.attemptNumber}.ª</td>
                        <td>{e.grade ?? '—'}</td>
                        <td>
                          <span className={`status status-${e.status.toLowerCase()}`}>
                            {STATUS_LABEL[e.status] ?? e.status}
                          </span>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </>
      )}
    </section>
  );
}

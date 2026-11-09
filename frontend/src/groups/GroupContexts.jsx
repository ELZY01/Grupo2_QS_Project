import { useCallback, useEffect, useState } from 'react';
import { createGroupContext, generateGroups, getGroupContexts, getGroups } from '../api/groupApi';
import { getStudents } from '../api/studentApi';
import GroupForming from '../mascot/GroupForming.jsx';
import { useMinimumDuration } from '../mascot/useMinimumDuration.js';
import GroupResults from './GroupResults.jsx';
import { describePlan, planGroups } from './groupPlan.js';
import './groups.css';

const formatDate = (value) => (value ? new Date(value).toLocaleString('pt-PT') : null);

/**
 * Área do administrador para os grupos (#74):
 * criar contexto, escolher os alunos elegíveis, gerar grupos e abrir gerações anteriores.
 */
export default function GroupContexts() {
  const [students, setStudents] = useState([]);
  const [contexts, setContexts] = useState([]);
  const [loadError, setLoadError] = useState(null);

  const [name, setName] = useState('');
  const [selected, setSelected] = useState(() => new Set());
  const [creating, setCreating] = useState(false);
  const [formError, setFormError] = useState(null);

  const [current, setCurrent] = useState(null);
  const [generation, setGeneration] = useState(null);
  const [generating, setGenerating] = useState(false);
  const [resultError, setResultError] = useState(null);
  const showForming = useMinimumDuration(generating);

  const loadContexts = useCallback(async () => {
    try {
      setContexts(await getGroupContexts());
    } catch (error) {
      setLoadError(error.message);
    }
  }, []);

  useEffect(() => {
    getStudents()
      .then(setStudents)
      .catch((error) => setLoadError(error.message));
    loadContexts();
  }, [loadContexts]);

  const toggle = (id) => {
    setSelected((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  };

  const toggleAll = () => {
    setSelected((prev) =>
      prev.size === students.length ? new Set() : new Set(students.map((s) => s.id)));
  };

  const count = selected.size;
  const possible = planGroups(count) !== null;

  const handleCreate = async (event) => {
    event.preventDefault();
    setFormError(null);
    if (!name.trim()) {
      setFormError('Indica o nome da atividade.');
      return;
    }
    if (!possible) {
      setFormError(describePlan(count));
      return;
    }
    setCreating(true);
    try {
      const created = await createGroupContext({ name: name.trim(), studentIds: [...selected] });
      setName('');
      setSelected(new Set());
      setContexts((list) => [created, ...list.filter((c) => c.id !== created.id)]);
      openContext(created);
    } catch (error) {
      setFormError(error.message);
    } finally {
      setCreating(false);
    }
  };

  const openContext = async (context) => {
    setCurrent(context);
    setGeneration(null);
    setResultError(null);
    if (!context.generatedAt) return;
    try {
      setGeneration(await getGroups(context.id));
    } catch (error) {
      setResultError(error.message);
    }
  };

  const handleGenerate = async () => {
    setGenerating(true);
    setResultError(null);
    try {
      const result = await generateGroups(current.id);
      setGeneration(result);
      const updated = { ...current, generatedAt: result.generatedAt ?? new Date().toISOString() };
      setCurrent(updated);
      setContexts((list) => list.map((c) => (c.id === updated.id ? updated : c)));
    } catch (error) {
      setResultError(error.message);
    } finally {
      setGenerating(false);
    }
  };

  return (
    <section className="group-contexts">
      {loadError && (
        <p className="group-error" role="alert">
          Não foi possível carregar os dados ({loadError}).
        </p>
      )}

      <div className="group-layout">
        <form className="card group-form" onSubmit={handleCreate} aria-label="Novo contexto">
          <h2>Novo contexto</h2>
          <label>
            Nome da atividade
            <input
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="ex.: Projeto final de QS"
            />
          </label>

          <fieldset className="group-students">
            <legend>Alunos elegíveis</legend>
            {students.length === 0 ? (
              <p>Ainda não há estudantes registados.</p>
            ) : (
              <>
                <label className="group-check group-check-all">
                  <input
                    type="checkbox"
                    checked={count === students.length}
                    onChange={toggleAll}
                  />
                  Todos ({students.length})
                </label>
                {students.map((s) => (
                  <label key={s.id} className="group-check">
                    <input type="checkbox" checked={selected.has(s.id)} onChange={() => toggle(s.id)} />
                    {s.name}
                    <span className="group-number">{s.studentNumber}</span>
                  </label>
                ))}
              </>
            )}
          </fieldset>

          <p className={`group-plan ${count > 0 && !possible ? 'warn' : ''}`} aria-live="polite">
            {describePlan(count)}
          </p>

          {formError && <p className="group-error" role="alert">{formError}</p>}

          <div className="actions">
            <button type="submit" className="btn-primary" disabled={creating}>
              {creating ? 'A criar…' : 'Criar contexto'}
            </button>
          </div>
        </form>

        <div className="card group-history">
          <h2>Contextos e gerações</h2>
          {contexts.length === 0 ? (
            <p>Ainda não há contextos.</p>
          ) : (
            <ul>
              {contexts.map((c) => (
                <li key={c.id}>
                  <button
                    type="button"
                    className={current?.id === c.id ? 'active' : ''}
                    aria-current={current?.id === c.id ? 'true' : undefined}
                    onClick={() => openContext(c)}
                  >
                    <span className="group-history-name">{c.name}</span>
                    <span className="group-history-meta">
                      {c.studentCount} alunos ·{' '}
                      {c.generatedAt ? `gerado em ${formatDate(c.generatedAt)}` : 'por gerar'}
                    </span>
                  </button>
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>

      {current && (
        <div className="card group-current">
          <header className="group-current-header">
            <div>
              <h2>{current.name}</h2>
              <p>
                {current.studentCount} alunos
                {current.generatedAt && ` · última geração em ${formatDate(current.generatedAt)}`}
              </p>
            </div>
            <button type="button" className="btn-primary" onClick={handleGenerate} disabled={showForming}>
              {current.generatedAt ? 'Gerar de novo' : 'Gerar grupos'}
            </button>
          </header>

          {resultError && <p className="group-error" role="alert">{resultError}</p>}

          {showForming ? (
            <GroupForming />
          ) : (
            generation && <GroupResults generation={generation} />
          )}

          {!showForming && !generation && !resultError && !current.generatedAt && (
            <p>Ainda não há grupos. Carrega em “Gerar grupos”.</p>
          )}
        </div>
      )}
    </section>
  );
}

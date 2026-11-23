import { useCallback, useEffect, useState } from 'react';
import { createGroupContext, generateGroups, getGroupContexts, getGroups, swapStudents } from '../api/groupApi';
import { getStudents } from '../api/studentApi';
import GroupForming from '../mascot/GroupForming.jsx';
import { useMinimumDuration } from '../mascot/useMinimumDuration.js';
import GroupEditor from './GroupEditor.jsx';
import GroupResults from './GroupResults.jsx';
import { describePlan, planGroups } from './groupPlan.js';
import './groups.css';

const formatDate = (value) => (value ? new Date(value).toLocaleString('pt-PT') : null);

/** Pedidos do admin: contextos sem UC (#69). */
const ADMIN_API = {
  students: getStudents,
  list: getGroupContexts,
  create: createGroupContext,
  generate: generateGroups,
  groups: getGroups,
  swap: swapStudents,
};

/** Texto curto de um contexto na lista: nº de alunos e estado dos grupos. */
function describeContext(context) {
  const parts = [];
  if (context.studentCount != null) parts.push(`${context.studentCount} alunos`);
  if (context.generatedAt) parts.push(`gerado em ${formatDate(context.generatedAt)}`);
  else if (context.generationNumber) parts.push('grupos feitos à mão');
  else parts.push('por gerar');
  return parts.join(' · ');
}

/**
 * Contextos e grupos: criar contexto, escolher os alunos elegíveis, gerar grupos e abrir gerações.
 *
 * - Admin (#74, por omissão): contextos sem UC e troca de alunos (#75).
 * - Docente (#106, `manage`): contextos de uma UC, estatísticas por grupo (R6) e ajuste manual
 *   dos grupos (criar, editar e apagar, R4/R5). O `api` vem de teacherGroupApi.
 *
 * @param {{ api?: object, manage?: boolean }} props
 */
export default function GroupContexts({ api = ADMIN_API, manage = false }) {
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
      setContexts(await api.list());
    } catch (error) {
      setLoadError(error.message);
    }
  }, [api]);

  useEffect(() => {
    api.students()
      .then(setStudents)
      .catch((error) => setLoadError(error.message));
    loadContexts();
  }, [api, loadContexts]);

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

  const updateCurrent = (changes) => {
    const updated = { ...current, ...changes };
    setCurrent(updated);
    setContexts((list) => list.map((c) => (c.id === updated.id ? updated : c)));
  };

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
      // O endpoint do admin não devolve o nº de alunos; já o sabemos.
      const created = { studentCount: count, ...(await api.create({ name: name.trim(), studentIds: [...selected] })) };
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

  const showGroups = async (load) => {
    setResultError(null);
    try {
      setGeneration(await load());
    } catch (error) {
      setResultError(error.message);
    }
  };

  const openContext = async (context) => {
    setCurrent(context);
    setGeneration(null);
    setResultError(null);
    // O docente abre sempre: mesmo sem geração, pode criar grupos à mão.
    if (!manage && !context.generatedAt) return;
    await showGroups(() => api.groups(context.id));
  };

  const openGeneration = (generationNumber) => showGroups(() => api.groups(current.id, generationNumber));

  const handleGenerate = async () => {
    setGenerating(true);
    setResultError(null);
    try {
      const result = await api.generate(current.id);
      setGeneration(result);
      updateCurrent({
        generatedAt: result.generatedAt ?? new Date().toISOString(),
        generationNumber: result.generationNumber,
      });
    } catch (error) {
      setResultError(error.message);
    } finally {
      setGenerating(false);
    }
  };

  // Ajuste manual do admin (#75): o backend devolve a geração com as médias recalculadas.
  const handleSwap = async (studentIdA, studentIdB) => {
    setGeneration(await api.swap(current.id, studentIdA, studentIdB));
  };

  // Ajuste manual do docente (#106): cada alteração devolve os grupos e as estatísticas novas.
  // Os erros (409) sobem para o GroupEditor, que os mostra.
  const handleEdit = async (request) => {
    const result = await request();
    setGeneration(result);
    if (result.generationNumber !== current.generationNumber) {
      updateCurrent({ generationNumber: result.generationNumber });
    }
  };

  const hasGroups = generation?.groups?.length > 0;
  const generations = generation?.generations ?? [];

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
              <p>{manage ? 'Ainda não há alunos inscritos nesta UC.' : 'Ainda não há estudantes registados.'}</p>
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
                    <span className="group-history-meta">{describeContext(c)}</span>
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
                {[
                  current.studentCount != null && `${current.studentCount} alunos`,
                  current.generatedAt && `última geração em ${formatDate(current.generatedAt)}`,
                ].filter(Boolean).join(' · ')}
              </p>
            </div>
            <button type="button" className="btn-primary" onClick={handleGenerate} disabled={showForming}>
              {current.generatedAt || hasGroups ? 'Gerar de novo' : 'Gerar grupos'}
            </button>
          </header>

          {!manage && generations.length > 1 && (
            <div className="group-generations" role="group" aria-label="Gerações">
              {generations.map((n) => (
                <button
                  key={n}
                  type="button"
                  className={n === generation.generationNumber ? 'active' : ''}
                  aria-pressed={n === generation.generationNumber}
                  onClick={() => openGeneration(n)}
                >
                  Geração {n}
                </button>
              ))}
            </div>
          )}

          {resultError && <p className="group-error" role="alert">{resultError}</p>}

          {showForming ? (
            <GroupForming />
          ) : (
            generation && (
              <>
                {hasGroups || !manage ? (
                  <GroupResults generation={generation} onSwap={manage ? undefined : handleSwap} />
                ) : (
                  <p>Ainda não há grupos. Carrega em “Gerar grupos” ou cria um grupo à mão.</p>
                )}
                {manage && (
                  <GroupEditor
                    key={`${current.id}-${generation.generationNumber}`}
                    generation={generation}
                    onCreate={(ids) => handleEdit(() => api.createGroup(current.id, ids))}
                    onUpdate={(groupId, ids) => handleEdit(() => api.updateGroup(current.id, groupId, ids))}
                    onDelete={(groupId) => handleEdit(() => api.deleteGroup(current.id, groupId))}
                  />
                )}
              </>
            )
          )}

          {!manage && !showForming && !generation && !resultError && !current.generatedAt && (
            <p>Ainda não há grupos. Carrega em “Gerar grupos”.</p>
          )}
        </div>
      )}
    </section>
  );
}

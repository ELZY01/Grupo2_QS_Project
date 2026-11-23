import { useState } from 'react';
import { MIN_GROUP_SIZE, groupSizeError } from './groupPlan.js';

const NEW_GROUP = 'new';

/**
 * Ajuste manual na área do docente (#106, R4/R5): criar um grupo com alunos sem grupo,
 * mudar os membros de um grupo ou apagá-lo.
 *
 * Um aluno não pode estar em dois grupos do mesmo contexto, por isso só se escolhem
 * os membros do próprio grupo e os alunos sem grupo. Para mudar um aluno de grupo:
 * tira-se do grupo onde está (fica sem grupo) e junta-se ao outro.
 * O backend valida tudo outra vez e responde 409 com a mensagem a mostrar.
 *
 * @param {{ generation: object, onCreate: Function, onUpdate: Function, onDelete: Function }} props
 */
export default function GroupEditor({ generation, onCreate, onUpdate, onDelete }) {
  const { groups = [], unassigned = [] } = generation;
  const [target, setTarget] = useState('');
  const [selected, setSelected] = useState(() => new Set());
  const [error, setError] = useState(null);
  const [saving, setSaving] = useState(false);

  const group = groups.find((g) => String(g.id) === target);
  const candidates = target === NEW_GROUP ? unassigned : group ? [...group.members, ...unassigned] : [];

  const choose = (value) => {
    setTarget(value);
    setError(null);
    const chosen = groups.find((g) => String(g.id) === value);
    setSelected(new Set(chosen ? chosen.members.map((m) => m.studentId) : []));
  };

  const toggle = (id) => {
    setSelected((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  };

  const run = async (action) => {
    setSaving(true);
    setError(null);
    try {
      await action();
      setTarget('');
      setSelected(new Set());
    } catch (err) {
      setError(err.message);
    } finally {
      setSaving(false);
    }
  };

  const handleSubmit = (event) => {
    event.preventDefault();
    const message = groupSizeError(selected.size);
    if (message) {
      setError(message);
      return;
    }
    const ids = [...selected];
    run(() => (target === NEW_GROUP ? onCreate(ids) : onUpdate(group.id, ids)));
  };

  return (
    <form className="group-editor" onSubmit={handleSubmit} aria-label="Editar grupos">
      <h3>Ajuste manual</h3>
      <p className="group-unassigned">
        {unassigned.length === 0
          ? 'Todos os alunos do contexto têm grupo.'
          : `Sem grupo (${unassigned.length}): ${unassigned.map((s) => s.name).join(', ')}`}
      </p>

      <label className="group-editor-target">
        Grupo a alterar
        <select value={target} onChange={(e) => choose(e.target.value)}>
          <option value="">—</option>
          {groups.map((g) => (
            <option key={g.id} value={String(g.id)}>Grupo {g.number}</option>
          ))}
          <option value={NEW_GROUP} disabled={unassigned.length < MIN_GROUP_SIZE}>
            Novo grupo{unassigned.length < MIN_GROUP_SIZE ? ` (precisa de ${MIN_GROUP_SIZE} alunos sem grupo)` : ''}
          </option>
        </select>
      </label>

      {target && (
        <>
          <fieldset className="group-students">
            <legend>Membros ({selected.size})</legend>
            {candidates.map((s) => (
              <label key={s.studentId} className="group-check">
                <input type="checkbox" checked={selected.has(s.studentId)} onChange={() => toggle(s.studentId)} />
                {s.name}
                <span className="group-number">{s.studentNumber}</span>
              </label>
            ))}
          </fieldset>

          <div className="group-editor-actions">
            <button type="submit" className="btn-primary" disabled={saving}>
              {target === NEW_GROUP ? 'Criar grupo' : 'Guardar membros'}
            </button>
            {group && (
              <button type="button" className="group-delete" disabled={saving} onClick={() => run(() => onDelete(group.id))}>
                Apagar grupo {group.number}
              </button>
            )}
          </div>
        </>
      )}

      {error && <p className="group-error" role="alert">{error}</p>}
    </form>
  );
}

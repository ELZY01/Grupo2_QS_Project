import { useCallback, useEffect, useState } from 'react';
import {
  assignTeacher,
  createTeacher,
  enrollStudent,
  getAssignments,
  getTeachers,
  removeStudent,
  removeTeacher,
} from '../api/adminApi';
import { getCourseUnits } from '../api/enrollmentApi';
import { getStudents } from '../api/studentApi';
import './CourseUnitAssignments.css';

const EMPTY_TEACHER = { displayName: '', username: '', password: '' };

/**
 * Admin: escolhe uma UC, associa docentes (R2) e inscreve alunos (R1).
 * É separado do histórico de notas, que continua em "Cadeiras e notas".
 */
export default function CourseUnitAssignments() {
  const [units, setUnits] = useState([]);
  const [teachers, setTeachers] = useState([]);
  const [students, setStudents] = useState([]);
  const [loadError, setLoadError] = useState(null);

  const [unitId, setUnitId] = useState('');
  const [assignments, setAssignments] = useState(null);
  const [teacherToAdd, setTeacherToAdd] = useState('');
  const [search, setSearch] = useState('');
  const [error, setError] = useState(null);
  const [busy, setBusy] = useState(false);

  const [newTeacher, setNewTeacher] = useState(EMPTY_TEACHER);
  const [teacherError, setTeacherError] = useState(null);
  const [teacherSuccess, setTeacherSuccess] = useState(null);

  useEffect(() => {
    Promise.all([getCourseUnits(), getTeachers(), getStudents()])
      .then(([u, t, s]) => {
        setUnits(u);
        setTeachers(t);
        setStudents(s);
      })
      .catch((err) => setLoadError(err.message));
  }, []);

  const loadAssignments = useCallback(async (id) => {
    setError(null);
    setAssignments(null);
    if (!id) return;
    try {
      setAssignments(await getAssignments(id));
    } catch (err) {
      setError(err.message);
    }
  }, []);

  const selectUnit = (id) => {
    setUnitId(id);
    setSearch('');
    setTeacherToAdd('');
    loadAssignments(id);
  };

  // Executa uma alteração e mostra o estado devolvido pelo backend.
  const run = async (action) => {
    setBusy(true);
    setError(null);
    try {
      setAssignments(await action());
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  };

  const handleCreateTeacher = async (e) => {
    e.preventDefault();
    setTeacherError(null);
    setTeacherSuccess(null);
    try {
      const created = await createTeacher({
        displayName: newTeacher.displayName.trim(),
        username: newTeacher.username.trim(),
        password: newTeacher.password,
      });
      setTeachers((list) => [...list, created].sort((a, b) => a.displayName.localeCompare(b.displayName)));
      setNewTeacher(EMPTY_TEACHER);
      setTeacherSuccess(`Docente ${created.displayName} criado.`);
    } catch (err) {
      setTeacherError(err.message);
    }
  };

  const assignedTeacherIds = new Set(assignments?.teachers.map((t) => t.id));
  const enrolledIds = new Set(assignments?.students.map((s) => s.id));
  const availableTeachers = teachers.filter((t) => !assignedTeacherIds.has(t.id));
  const query = search.trim().toLowerCase();
  const candidates = query
    ? students
        .filter((s) => !enrolledIds.has(s.id))
        .filter((s) => `${s.name} ${s.studentNumber} ${s.email}`.toLowerCase().includes(query))
        .slice(0, 8)
    : [];

  return (
    <section className="assignments">
      {loadError && (
        <p className="assign-error" role="alert">
          Não foi possível carregar os dados ({loadError}).
        </p>
      )}

      <div className="card assign-picker">
        <label>
          Unidade curricular
          <select value={unitId} onChange={(e) => selectUnit(e.target.value)}>
            <option value="">Escolhe uma UC…</option>
            {units.map((u) => (
              <option key={u.id} value={u.id}>
                {u.code} · {u.name}
              </option>
            ))}
          </select>
        </label>
      </div>

      {error && (
        <p className="assign-error" role="alert">
          {error}
        </p>
      )}

      {assignments && (
        <div className="assign-grid">
          <div className="card">
            <h2>Docentes</h2>
            {assignments.teachers.length === 0 ? (
              <p className="assign-empty">Ainda não há docentes nesta UC.</p>
            ) : (
              <ul className="assign-list">
                {assignments.teachers.map((t) => (
                  <li key={t.id}>
                    <span>
                      {t.displayName} <span className="assign-muted">@{t.username}</span>
                    </span>
                    <button
                      type="button"
                      disabled={busy}
                      onClick={() => run(() => removeTeacher(unitId, t.id))}
                      aria-label={`Remover ${t.displayName}`}
                    >
                      Remover
                    </button>
                  </li>
                ))}
              </ul>
            )}

            <div className="assign-add">
              <label>
                Associar docente
                <select value={teacherToAdd} onChange={(e) => setTeacherToAdd(e.target.value)}>
                  <option value="">Escolhe um docente…</option>
                  {availableTeachers.map((t) => (
                    <option key={t.id} value={t.id}>
                      {t.displayName}
                    </option>
                  ))}
                </select>
              </label>
              <button
                type="button"
                className="btn-primary"
                disabled={!teacherToAdd || busy}
                onClick={() => run(async () => {
                  const result = await assignTeacher(unitId, Number(teacherToAdd));
                  setTeacherToAdd('');
                  return result;
                })}
              >
                Associar
              </button>
            </div>
          </div>

          <div className="card">
            <h2>Alunos inscritos ({assignments.students.length})</h2>
            <label>
              Inscrever aluno
              <input
                type="search"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                placeholder="Nome, número ou email"
              />
            </label>
            {candidates.length > 0 && (
              <ul className="assign-list assign-candidates" aria-label="Alunos encontrados">
                {candidates.map((s) => (
                  <li key={s.id}>
                    <span>
                      {s.name} <span className="assign-muted">{s.studentNumber}</span>
                    </span>
                    <button
                      type="button"
                      disabled={busy}
                      onClick={() => run(async () => {
                        const result = await enrollStudent(unitId, s.id);
                        setSearch('');
                        return result;
                      })}
                      aria-label={`Inscrever ${s.name}`}
                    >
                      Inscrever
                    </button>
                  </li>
                ))}
              </ul>
            )}
            {query && candidates.length === 0 && <p className="assign-empty">Nenhum aluno por inscrever com esse texto.</p>}

            {assignments.students.length === 0 ? (
              <p className="assign-empty">Ainda não há alunos inscritos nesta UC.</p>
            ) : (
              <ul className="assign-list">
                {assignments.students.map((s) => (
                  <li key={s.id}>
                    <span>
                      {s.name} <span className="assign-muted">{s.studentNumber}</span>
                    </span>
                    <button
                      type="button"
                      disabled={busy}
                      onClick={() => run(() => removeStudent(unitId, s.id))}
                      aria-label={`Remover ${s.name}`}
                    >
                      Remover
                    </button>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </div>
      )}

      <form className="card assign-teacher-form" onSubmit={handleCreateTeacher} aria-label="Novo docente">
        <h2>Novo docente</h2>
        <div className="assign-row">
          <label>
            Nome
            <input
              value={newTeacher.displayName}
              onChange={(e) => setNewTeacher((v) => ({ ...v, displayName: e.target.value }))}
            />
          </label>
          <label>
            Utilizador
            <input
              value={newTeacher.username}
              onChange={(e) => setNewTeacher((v) => ({ ...v, username: e.target.value }))}
              autoComplete="off"
            />
          </label>
          <label>
            Palavra-passe inicial
            <input
              type="password"
              value={newTeacher.password}
              onChange={(e) => setNewTeacher((v) => ({ ...v, password: e.target.value }))}
              autoComplete="new-password"
              placeholder="Mínimo 8 caracteres"
            />
          </label>
        </div>
        {teacherError && <p className="assign-error" role="alert">{teacherError}</p>}
        {teacherSuccess && <p className="assign-success" role="status">{teacherSuccess}</p>}
        <div className="actions">
          <button type="submit" className="btn-primary">Criar docente</button>
        </div>
      </form>
    </section>
  );
}

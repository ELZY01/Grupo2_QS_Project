import { useEffect, useState } from 'react';
import { getCourseUnitStudents, getMyCourseUnits } from '../api/teacherApi';
import AcademicHistory from '../components/AcademicHistory';
import CompetencyProfile from '../components/CompetencyProfile';
import '../App.css';
import './teacher.css';

const countStudents = (n) => `${n} ${n === 1 ? 'aluno' : 'alunos'}`;

const SECTIONS = [
  { id: 'students', label: 'Alunos' },
  { id: 'groups', label: 'Grupos' },
];

/**
 * Área do docente (R3, R6):
 * as suas UCs → alunos inscritos → estatísticas de cada aluno (perfil e histórico).
 * Os grupos ficam disponíveis quando o backend dos grupos estiver pronto (#68/#69).
 */
export default function TeacherApp({ name, onLogout }) {
  const [units, setUnits] = useState(null);
  const [loadError, setLoadError] = useState(null);
  const [unitId, setUnitId] = useState(null);
  const [section, setSection] = useState('students');

  useEffect(() => {
    getMyCourseUnits()
      .then((list) => {
        setUnits(list);
        if (list.length > 0) setUnitId(list[0].courseUnit.id);
      })
      .catch((err) => setLoadError(err.message));
  }, []);

  const current = units?.find((u) => u.courseUnit.id === unitId);

  return (
    <div className="app-shell">
      <aside className="app-sidebar">
        <div className="app-brand">
          <span className="app-logo" aria-hidden="true">SM</span>
          <div>
            <p className="app-name">StudyMatch</p>
            <p className="app-tagline">Docente</p>
          </div>
        </div>

        <nav className="app-nav" aria-label="As minhas UCs">
          <p className="teacher-nav-title">As minhas UCs</p>
          {units?.map(({ courseUnit, studentCount }) => (
            <button
              key={courseUnit.id}
              type="button"
              className={unitId === courseUnit.id ? 'active' : ''}
              aria-current={unitId === courseUnit.id ? 'page' : undefined}
              onClick={() => {
                setUnitId(courseUnit.id);
                setSection('students');
              }}
            >
              {courseUnit.name}
              <span className="teacher-count">{countStudents(studentCount)}</span>
            </button>
          ))}
        </nav>
      </aside>

      <main className="app-main">
        <header className="page-header admin-header">
          <div>
            <h1>{current ? current.courseUnit.name : `Olá, ${name}`}</h1>
            <p>{current ? `${current.courseUnit.code} · ${countStudents(current.studentCount)} ${current.studentCount === 1 ? 'inscrito' : 'inscritos'}` : 'Área do docente'}</p>
          </div>
          <button type="button" className="admin-logout" onClick={onLogout}>
            Sair
          </button>
        </header>

        {loadError && (
          <p className="teacher-error" role="alert">
            Não foi possível carregar as tuas UCs ({loadError}).
          </p>
        )}

        {units && units.length === 0 && (
          <div className="card teacher-empty">
            <h2>Ainda não tens unidades curriculares</h2>
            <p>Quando o administrador te associar a uma UC, ela aparece aqui com os alunos inscritos.</p>
          </div>
        )}

        {current && (
          <>
            <div className="teacher-tabs" role="tablist" aria-label="Secções da UC">
              {SECTIONS.map((s) => (
                <button
                  key={s.id}
                  type="button"
                  role="tab"
                  aria-selected={section === s.id}
                  className={section === s.id ? 'active' : ''}
                  onClick={() => setSection(s.id)}
                >
                  {s.label}
                </button>
              ))}
            </div>

            {section === 'students' && <CourseUnitStudents key={current.courseUnit.id} courseUnitId={current.courseUnit.id} />}
            {section === 'groups' && (
              <div className="card teacher-empty">
                <h2>Grupos</h2>
                <p>
                  A criação e as estatísticas dos grupos ficam disponíveis quando a formação de grupos
                  estiver pronta no backend.
                </p>
              </div>
            )}
          </>
        )}
      </main>
    </div>
  );
}

/**
 * Alunos inscritos numa UC do docente e as estatísticas do aluno escolhido.
 */
function CourseUnitStudents({ courseUnitId }) {
  const [students, setStudents] = useState(null);
  const [error, setError] = useState(null);
  const [selected, setSelected] = useState(null);

  useEffect(() => {
    getCourseUnitStudents(courseUnitId)
      .then(setStudents)
      .catch((err) => setError(err.message));
  }, [courseUnitId]);

  if (error) {
    return <p className="teacher-error" role="alert">{error}</p>;
  }
  if (!students) {
    return <p>A carregar alunos…</p>;
  }
  if (students.length === 0) {
    return (
      <div className="card teacher-empty">
        <p>Ainda não há alunos inscritos nesta UC. O administrador é quem inscreve os alunos.</p>
      </div>
    );
  }

  return (
    <div className="teacher-layout">
      <div className="card teacher-students">
        <h2>Alunos inscritos</h2>
        <ul>
          {students.map((s) => (
            <li key={s.id}>
              <button
                type="button"
                className={selected?.id === s.id ? 'active' : ''}
                aria-current={selected?.id === s.id ? 'true' : undefined}
                onClick={() => setSelected(s)}
              >
                <span className="teacher-student-name">{s.name}</span>
                <span className="teacher-count">{s.studentNumber} · {s.course}, {s.academicYear}.º ano</span>
              </button>
            </li>
          ))}
        </ul>
      </div>

      <div className="teacher-detail">
        {selected ? (
          <>
            <div className="card">
              <h2>{selected.name}</h2>
              <p className="teacher-count">{selected.email}</p>
            </div>
            <CompetencyProfile key={`p-${selected.id}`} studentId={selected.id} forStudent={false} />
            <AcademicHistory
              key={`h-${selected.id}`}
              studentId={selected.id}
              readOnly
              emptyText="Este aluno ainda não tem cadeiras registadas."
            />
          </>
        ) : (
          <div className="card teacher-empty">
            <p>Escolhe um aluno para ver as competências, as notas e o histórico.</p>
          </div>
        )}
      </div>
    </div>
  );
}

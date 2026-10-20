import { useState } from 'react';
import HealthStatus from './components/HealthStatus';
import { QuizOnboarding } from './components/QuizOnboarding';
import StudentManager from './components/StudentManager';
import StudentRecords from './components/StudentRecords';
import MascotGuide from './mascot/MascotGuide.jsx';
import './App.css';

// Cada separador mostra uma área da aplicação, em vez de estar tudo na mesma página.
const TABS = [
  { id: 'students', label: 'Estudantes', hint: 'Registar e editar estudantes' },
  { id: 'records', label: 'Percurso e perfil', hint: 'Notas, competências e mascote' },
  { id: 'quiz', label: 'Quiz inicial', hint: 'Perfil para quem ainda não tem notas' },
];

export default function App() {
  const [tab, setTab] = useState('students');
  const [quizDone, setQuizDone] = useState(false);
  const [selectedStudentId, setSelectedStudentId] = useState(null);
  const current = TABS.find((t) => t.id === tab);

  return (
    <div className="app-shell">
      <aside className="app-sidebar">
        <div className="app-brand">
          <span className="app-logo" aria-hidden="true">SM</span>
          <div>
            <p className="app-name">StudyMatch</p>
            <p className="app-tagline">Grupos equilibrados por competências</p>
          </div>
        </div>

        <nav className="app-nav" aria-label="Secções">
          {TABS.map((t) => (
            <button
              key={t.id}
              type="button"
              className={tab === t.id ? 'active' : ''}
              aria-current={tab === t.id ? 'page' : undefined}
              onClick={() => setTab(t.id)}
            >
              {t.label}
            </button>
          ))}
        </nav>

        <MascotGuide tab={tab} />

        <div className="app-status">
          <HealthStatus />
        </div>
      </aside>

      <main className="app-main">
        <header className="page-header">
          <h1>{current.label}</h1>
          <p>{current.hint}</p>
        </header>

        {tab === 'students' && <StudentManager />}

        {tab === 'records' && (
          <StudentRecords
            selectedStudentId={selectedStudentId}
            onSelectStudent={setSelectedStudentId}
          />
        )}

        {tab === 'quiz' &&
          (quizDone ? (
            <section className="card quiz-done">
              <h2>Quiz submetido</h2>
              <p>As competências foram registadas. Vê o resultado em &quot;Percurso e perfil&quot;.</p>
              <div className="actions">
                <button type="button" className="btn-primary" onClick={() => setTab('records')}>
                  Ver perfil
                </button>
                <button type="button" onClick={() => setQuizDone(false)}>
                  Refazer quiz
                </button>
              </div>
            </section>
          ) : (
            <QuizOnboarding
              studentId={selectedStudentId}
              onSelectStudent={setSelectedStudentId}
              onComplete={() => setQuizDone(true)}
            />
          ))}
      </main>
    </div>
  );
}
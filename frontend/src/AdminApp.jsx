import { useState } from 'react';
import HealthStatus from './components/HealthStatus';
import StudentManager from './components/StudentManager';
import StudentRecords from './components/StudentRecords';
import AdminPix from './mascot/AdminPix.jsx';
import './App.css';

const TABS = [
  { id: 'students', label: 'Estudantes', hint: 'Registar e editar estudantes' },
  { id: 'records', label: 'Cadeiras e notas', hint: 'Lançar as UCs e notas de cada estudante' },
];

/**
 * Área do administrador: gere estudantes e lança as cadeiras e notas.
 * Não tem quiz: o Pix do administrador é a versão ultra rara.
 */
export default function AdminApp({ onLogout }) {
  const [tab, setTab] = useState('students');
  const current = TABS.find((t) => t.id === tab);

  return (
    <div className="app-shell">
      <aside className="app-sidebar">
        <div className="app-brand">
          <span className="app-logo" aria-hidden="true">SM</span>
          <div>
            <p className="app-name">StudyMatch</p>
            <p className="app-tagline">Administração</p>
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

        <AdminPix tab={tab} />

        <div className="app-status">
          <HealthStatus />
        </div>
      </aside>

      <main className="app-main">
        <header className="page-header admin-header">
          <div>
            <h1>{current.label}</h1>
            <p>{current.hint}</p>
          </div>
          <button type="button" className="admin-logout" onClick={onLogout}>
            Sair
          </button>
        </header>

        {tab === 'students' && <StudentManager />}
        {tab === 'records' && <StudentRecords />}
      </main>
    </div>
  );
}
